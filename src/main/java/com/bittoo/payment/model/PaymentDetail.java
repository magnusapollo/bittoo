package com.bittoo.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stripe.model.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDetail {
  String id;
  CreditCard creditCard;
  String customerId;

  @Builder
  @Data
  public static class CreditCard {
    /**
     * Card brand. Can be {@code amex}, {@code diners}, {@code discover}, {@code jcb}, {@code
     * mastercard}, {@code unionpay}, {@code visa}, or {@code unknown}.
     */
    @JsonProperty("brand")
    String brand;
    /** Checks on Card address and CVC if provided. */
    @JsonProperty("checks")
    PaymentMethod.Card.Checks checks;
    /**
     * Two-letter ISO code representing the country of the card. You could use this attribute to get
     * a sense of the international breakdown of cards you've collected.
     */
    @JsonProperty("country")
    String country;
    /**
     * A high-level description of the type of cards issued in this range. (For internal use only
     * and not typically available in standard API requests.)
     */
    @JsonProperty("description")
    String description;
    /** Two-digit number representing the card's expiration month. */
    @JsonProperty("exp_month")
    Long expMonth;
    /** Four-digit number representing the card's expiration year. */
    @JsonProperty("exp_year")
    Long expYear;
    /**
     * Uniquely identifies this particular card number. You can use this attribute to check whether
     * two customers who’ve signed up with you are using the same card number, for example. For
     * payment methods that tokenize card information (Apple Pay, Google Pay), the tokenized number
     * might be provided instead of the underlying card number.
     *
     * <p><em>Starting May 1, 2021, card fingerprint in India for Connect will change to allow two
     * fingerprints for the same card --- one for India and one for the rest of the world.</em>
     */
    @JsonProperty("fingerprint")
    String fingerprint;
    /**
     * Card funding type. Can be {@code credit}, {@code debit}, {@code prepaid}, or {@code unknown}.
     */
    @JsonProperty("funding")
    String funding;
    /**
     * Issuer identification number of the card. (For internal use only and not typically available
     * in standard API requests.)
     */
    @JsonProperty("iin")
    String iin;
    /**
     * The name of the card's issuing bank. (For internal use only and not typically available in
     * standard API requests.)
     */
    @JsonProperty("issuer")
    String issuer;
    /** The last four digits of the card. */
    @JsonProperty("last4")
    String last4;
    /** Contains information about card networks that can be used to process the payment. */
    @JsonProperty("networks")
    PaymentMethod.Card.Networks networks;
    /** Contains details on how this Card maybe be used for 3D Secure authentication. */
    @JsonProperty("three_d_secure_usage")
    PaymentMethod.Card.ThreeDSecureUsage threeDSecureUsage;
    /** If this Card is part of a card wallet, this contains the details of the card wallet. */
    @JsonProperty("wallet")
    PaymentMethod.Card.Wallet wallet;

    public static class Checks {
      /**
       * If a address line1 was provided, results of the check, one of {@code pass}, {@code fail},
       * {@code unavailable}, or {@code unchecked}.
       */
      @JsonProperty("address_line1_check")
      String addressLine1Check;
      /**
       * If a address postal code was provided, results of the check, one of {@code pass}, {@code
       * fail}, {@code unavailable}, or {@code unchecked}.
       */
      @JsonProperty("address_postal_code_check")
      String addressPostalCodeCheck;
      /**
       * If a CVC was provided, results of the check, one of {@code pass}, {@code fail}, {@code
       * unavailable}, or {@code unchecked}.
       */
      @JsonProperty("cvc_check")
      String cvcCheck;
    }
  }
}
