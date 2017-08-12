package rocks.appconcept.javatools.condition;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Build condition statements for avoiding doing it using the try-catch block.
 * Idea taken from:
 * the book Effective Java,
 * the paragraph 'Avoid unnecessary use of checked exceptions'
 * the page 247
 *
 * Replaces
 *  try{
 *
 *  } catch(){
 *
 *  }
 *
 *  with
 *    Condition<String> condition = new Condition<>(Long::parseLong);
 *    if(condition.isValid("5")) {
 *
 *    } else {
 *
 *    }
 *
 *  or
 *    Condition<Long> condition = new Condition<>(() -> Long.parseLong("1"));
 *    if(condition.isValid()) {
 *
 *    } else {
 *
 *    }
 *
 * @author yanimetaxas
 */
class Condition<T> {
  private Consumer<T> consumer;
  private Supplier<T> supplier;

  public Condition(Consumer<T> consumer) {
    this.consumer = consumer;
  }

  public Condition(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  public boolean isValid(T value) {
    try {
      consumer.accept(value);
    } catch (Exception e){
      return false;
    }
    return true;
  }

  public boolean isValid() {
    try {
      return supplier.get() != null;
    } catch (Exception e){
      return false;
    }
  }

  public T get() {
    if(supplier != null) {
      return supplier.get();
    }
    return null;
  }
}
