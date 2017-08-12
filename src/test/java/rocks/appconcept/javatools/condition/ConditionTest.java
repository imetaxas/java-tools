package rocks.appconcept.javatools.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author yanimetaxas
 */
public class ConditionTest {

  @Test
  public void isValid_ValidConsumer() throws Exception {
    Condition<String> condition = new Condition<>(Long::parseLong);

    assertTrue(condition.isValid("5"));
  }

  @Test
  public void isValid_NonValidConsumer() throws Exception {
    Condition<String> condition = new Condition<>(Long::parseLong);

    assertFalse(condition.isValid(""));
  }

  @Test
  public void isValid_ValidSupplier() throws Exception {
    Condition<Long> condition = new Condition<>(() -> Long.parseLong("1"));

    assertTrue(condition.isValid());
  }

  @Test
  public void isNonValid_NSupplier() throws Exception {
    Condition<Long> condition = new Condition<>(() -> Long.parseLong(""));

    assertFalse(condition.isValid());
  }

  @Test
  public void get() throws Exception {
    Condition<Long> condition = new Condition<>(() -> Long.parseLong("1"));

    assertNotNull(condition.get());
  }

  @Test
  public void get_SupplierIsNull() throws Exception {
    Condition<String> condition = new Condition<>(Long::parseLong);

    assertNull(condition.get());
  }

}