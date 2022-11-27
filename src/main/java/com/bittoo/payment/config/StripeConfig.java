package com.bittoo.payment.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "stripe")
public interface StripeConfig {

  String publicKey();

  String secretKey();
}
