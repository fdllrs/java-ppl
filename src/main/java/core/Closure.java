package core;

import ast.Expression;

import java.util.List;

public record Closure(List<String> params, List<Expression> body, Environment environment) {

}
