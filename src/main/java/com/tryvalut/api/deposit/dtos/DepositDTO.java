package com.tryvalut.api.deposit.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositDTO(@JsonProperty("id") Integer depositId, @JsonProperty("customer_id") Integer customerId,
                         @JsonProperty("load_amount")
                         @JsonDeserialize(using = CurrencyDeserializer.class) Double loadAmount,
                         @JsonProperty("time") @JsonDeserialize(using = LocalDateTimeDeserializer.class) LocalDateTime requestTime) {

    private static class CurrencyDeserializer extends JsonDeserializer<Double> {
        @Override
        public Double deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            String value = jsonParser.readValueAs(String.class);
            String currencyString = value.replace("$", "");
            BigDecimal currencyValue = new BigDecimal(currencyString);
            return currencyValue.doubleValue();
        }
    }
}
