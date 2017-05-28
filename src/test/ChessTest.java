package test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ChessTest {

    @Test
    public void multiplicationOfZeroIntegersShouldReturnZero() {
    	ChessTest tester = new ChessTest(); // MyClass is tested

        // assert statements
        assertEquals("10 x 0 must be 0", 0, tester.multiply(10, 0));
        assertEquals("0 x 10 must be 0", 0, tester.multiply(0, 10));
        assertEquals("0 x 0 must be 0", 0, tester.multiply(0, 0));
    }
}