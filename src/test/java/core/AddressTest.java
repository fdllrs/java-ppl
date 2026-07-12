package core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AddressTest {

	@Test
	public void testEmptyAddress() {
		Address addr = new Address();
		assertTrue(addr.path().isEmpty());
	}

	@Test
	public void testInmutableAddresses() {
		Address addr = new Address();
		Address newAddr = addr.append(AddressTag.LET);
		assertEquals(List.of(AddressTag.LET), newAddr.path());
		assertTrue(addr.path().isEmpty());
	}

	@Test
	public void testAppendMultipleElements() {
		Address addr = new Address().append(AddressTag.FN, 1);
		assertEquals(List.of(AddressTag.FN, 1), addr.path());

		Address nestedAddr = addr.append(AddressTag.BODY, 0);
		assertEquals(List.of(AddressTag.FN, 1, AddressTag.BODY, 0), nestedAddr.path());
	}

	@Test
	public void testAddressEquality() {
		Address addr1 = new Address().append(AddressTag.FN, 1);
		Address addr2 = new Address().append(AddressTag.FN, 1);
		Address addr3 = new Address().append(AddressTag.FN, 2);

		assertEquals(addr1, addr2);
		assertNotEquals(addr1, addr3);
		assertEquals(addr1, addr2);
	}

	@Test
	public void testHashCodeConsistency() {
		Address a = new Address().append(AddressTag.FN, 1);
		Address b = new Address().append(AddressTag.FN, 1);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testPrefixNotEqualToFullAddress() {
		Address prefix = new Address().append(AddressTag.FN);
		Address full = new Address().append(AddressTag.FN, AddressTag.BODY);
		assertNotEquals(prefix, full);
	}

	@Test
	public void testToStringNonEmpty() {
		Address addr = new Address().append(AddressTag.LET, 0);
		String str = addr.toString();
		assertFalse(str.isEmpty());
	}

	@Test
	public void testEmptyAddressEqualsSelf() {
		Address a = new Address();
		Address b = new Address();
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}
}
