package com.brewledger.brewledger.backend;

import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionItemRequest;
import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionRequest;
import com.brewledger.brewledger.backend.enums.PaymentMethod;
import com.brewledger.brewledger.backend.enums.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRequestValidationTests {

    private final Validator validator;

    TransactionRequestValidationTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void validatesNestedTransactionItems() {
        CreateTransactionItemRequest invalidItem = new CreateTransactionItemRequest();
        invalidItem.setProductId(1L);
        invalidItem.setQuantity(0);

        CreateTransactionRequest request = validRequest();
        request.setItems(List.of(invalidItem));

        Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("items[0].quantity");
    }

    @Test
    void rejectsNegativeTransactionAmounts() {
        CreateTransactionRequest request = validRequest();
        request.setDiscountAmount(-1.0);
        request.setCashReceived(-1.0);

        Set<ConstraintViolation<CreateTransactionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("discountAmount", "cashReceived");
    }

    private CreateTransactionRequest validRequest() {
        CreateTransactionItemRequest item = new CreateTransactionItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionType(TransactionType.TAKE_AWAY);
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setCashReceived(20_000.0);
        request.setItems(List.of(item));
        return request;
    }
}
