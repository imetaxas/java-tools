package rocks.appconcept.javatools.equality;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Tests objects' equals methods and hashCode
 *
 * @author yanimetaxas
 * @since 11-Feb-18
 */
public final class Money implements Serializable, Comparable<Money> {
  private static final long serialVersionUID = 1L;

  private BigDecimal amount;
  private Currency currency;

  public Money(BigDecimal amount, Currency currency) {
    this.amount = amount;
    this.currency = currency;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public static Money toMoney(String amount, String currency) {
    return new Money(new BigDecimal(amount), Currency.getInstance(currency));
  }

  @Override
  public int compareTo(Money obj) {
    return getAmount().compareTo(obj.getAmount());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Money money = (Money) o;

    return amount.equals(money.amount) && currency.equals(money.currency);
  }

  @Override
  public int hashCode() {
    int result = amount.hashCode();
    result = 31 * result + currency.hashCode();
    return result;
  }
}
