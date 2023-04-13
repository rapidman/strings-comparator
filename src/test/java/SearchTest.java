import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.test.Search;
import org.junit.Test;

/**
 * @author timur
 * @since 13.04.2023
 */
public class SearchTest {

  @Test
  public void testEquals_50Percent() {
    String input = "Малый пер.";
    String compareWith = "Малый пр.";
    double ratio = Search.getStringDiffRatio(input, compareWith);
    assertEquals(0.5, ratio, 0);
  }

  @Test
  public void testEquals_100Percent() {
    String input = "Малый пер.";
    String compareWith = "Малый пер.";
    double ratio = Search.getStringDiffRatio(input, compareWith);
    assertEquals(1.0, ratio, 0);
  }
}
