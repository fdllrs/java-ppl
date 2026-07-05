package core;

import Instructions.LetK.Binding;
import ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parser {

	public static List<Token> tokenize(String text) {
		List<Token> tokens = new ArrayList<>();
		int i = 0;
		int n = text.length();
		while (i < n) {
			char c = text.charAt(i);
			if (Character.isWhitespace(c) || c == ',') {
				i++;
			}
			else if (c == ';') {
				while (i < n && text.charAt(i) != '\n') {
					i++;
				}
			}
			else if (c == '(' || c == '[') {
				tokens.add(new Token(Token.Type.LPAREN, "("));
				i++;
			}
			else if (c == ')' || c == ']') {
				tokens.add(new Token(Token.Type.RPAREN, ")"));
				i++;
			}
			else if (c == '"') {
				int j = i + 1;
				StringBuilder buf = new StringBuilder();
				while (j < n && text.charAt(j) != '"') {
					if (text.charAt(j) == '\\' && j + 1 < n) {
						j++;
					}
					buf.append(text.charAt(j));
					j++;
				}
				if (j >= n) {
					throw new RuntimeException("Unterminated string literal");
				}
				tokens.add(new Token(Token.Type.STRING, buf.toString()));
				i = j + 1;
			}
			else {
				int j = i;
				while (j < n) {
					char curr = text.charAt(j);
					if (Character.isWhitespace(curr) || curr == ',' || curr == '(' || curr == ')' ||
						curr == '[' || curr == ']' || curr == ';' || curr == '"') {
						break;
					}
					j++;
				}
				tokens.add(new Token(Token.Type.ATOM, text.substring(i, j)));
				i = j;
			}
		}
		return tokens;
	}

	private static ReadResult readForm(List<Token> tokens, int pos) {
		if (pos >= tokens.size()) {
			throw new RuntimeException("Unexpected end of input");
		}
		Token tok = tokens.get(pos);
		if (tok.type == Token.Type.LPAREN) {
			List<Object> form = new ArrayList<>();
			pos++;
			while (true) {
				if (pos >= tokens.size()) {
					throw new RuntimeException("Missing closing parenthesis");
				}
				if (tokens.get(pos).type == Token.Type.RPAREN) {
					return new ReadResult(form, pos + 1);
				}
				ReadResult result = readForm(tokens, pos);
				form.add(result.value);
				pos = result.nextPos;
			}
		}
		if (tok.type == Token.Type.RPAREN) {
			throw new RuntimeException("Unexpected )");
		}
		if (tok.type == Token.Type.STRING) {
			return new ReadResult(tok.text, pos + 1);
		}
		return new ReadResult(parseAtom(tok.text), pos + 1);
	}

	private static Object parseAtom(String text) {
		switch (text) {
			case "true" -> {
				return Boolean.TRUE;
			}
			case "false" -> {
				return Boolean.FALSE;
			}
			case "nil" -> {
				return null;
			}
		}
		try {
			return Long.parseLong(text);
		} catch (NumberFormatException e) {
			// ignore
		}
		try {
			return Double.parseDouble(text);
		} catch (NumberFormatException e) {
			// ignore
		}
		return new Symbol(text);
	}

	public static List<Object> parseForms(String text) {
		List<Token> tokens = tokenize(text);
		List<Object> forms = new ArrayList<>();
		int pos = 0;
		while (pos < tokens.size()) {
			ReadResult result = readForm(tokens, pos);
			forms.add(result.value);
			pos = result.nextPos;
		}
		return forms;
	}

	public static Expression toExpression(Object form) {
		if (form instanceof Symbol(String aName)) {
			return new SymbolExpression(aName);
		}
		if (!( form instanceof List<?> list )) {
			return new ValueExpression(form);
		}
		if (list.isEmpty()) {
			return new ValueExpression(Collections.emptyList());
		}

		Object headObj = list.get(0);
		if (headObj instanceof Symbol(String head)) {
			switch (head) {
				case "let" -> {
					List<?> bindsList = (List<?>) list.get(1);
					List<Binding> binds = new ArrayList<>();
					for (int i = 0; i < bindsList.size(); i += 2) {
						Symbol varSym = (Symbol) bindsList.get(i);
						Expression valExpr = toExpression(bindsList.get(i + 1));
						binds.add(new Binding(varSym.name, valExpr));
					}
					List<Expression> body = new ArrayList<>();
					for (int i = 2; i < list.size(); i++) {
						body.add(toExpression(list.get(i)));
					}
					return new LetExpression(binds, body);
				}
				case "if" -> {
					Expression test = toExpression(list.get(1));
					Expression then = toExpression(list.get(2));
					Expression els = toExpression(list.get(3));
					return new IfExpression(test, then, els);
				}
				case "fn" -> {
					List<?> paramsList = (List<?>) list.get(1);
					List<String> params = new ArrayList<>();
					for (Object p : paramsList) {
						params.add(( (Symbol) p ).name);
					}
					List<Expression> body = new ArrayList<>();
					for (int i = 2; i < list.size(); i++) {
						body.add(toExpression(list.get(i)));
					}
					return new FnExpression(params, body);
				}
				case "sample" -> {
					Expression dist = toExpression(list.get(1));
					return new SampleExpression(dist);
				}
				case "observe" -> {
					Expression dist = toExpression(list.get(1));
					Expression val = toExpression(list.get(2));
					return new ObserveExpression(dist, val);
				}
				case "defn" -> {
					String name = ( (Symbol) list.get(1) ).name;
					List<?> paramsList = (List<?>) list.get(2);
					List<String> params = new ArrayList<>();
					for (Object p : paramsList) {
						params.add(( (Symbol) p ).name);
					}
					List<Expression> body = new ArrayList<>();
					for (int i = 3; i < list.size(); i++) {
						body.add(toExpression(list.get(i)));
					}
					return new DefnExpression(name, params, body);
				}
			}
		}

		// Default function/operator call
		Expression operator = toExpression(list.get(0));
		List<Expression> operands = new ArrayList<>();
		for (int i = 1; i < list.size(); i++) {
			operands.add(toExpression(list.get(i)));
		}
		return new CallExpression(operator, operands);
	}

	public static List<Expression> parse(String text) {
		List<Object> forms = parseForms(text);
		List<Expression> parsedExpressions = new ArrayList<>();
		for (Object f : forms) {
			parsedExpressions.add(toExpression(f));
		}
		return parsedExpressions;
	}

	public record Token(Type type, String text) {
		@Override
		public String toString() {
			return String.format("%s:%s", type, text);
		}

		public enum Type {
			LPAREN, RPAREN, STRING, ATOM
		}
	}

	public record Symbol(String name) {

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!( o instanceof Symbol(String aName) )) return false;
			return name.equals(aName);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private record ReadResult(Object value, int nextPos) { }
}
