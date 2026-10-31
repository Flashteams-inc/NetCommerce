package com.netcommerce.kernel.core.results;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Results and errors have the JSON shape the .NET ones have on the wire (captured from .NET). */
class ResultJsonTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void anErrorHasTheSameFieldsAndValues() throws Exception {
        assertThat(json.readTree(json.writeValueAsString(Error.notFound("Product", 42))))
                .isEqualTo(json.readTree("{\"code\":\"Product.NotFound\",\"description\":\"Product with id \\u002742\\u0027 was not found\",\"statusCode\":404}"));
        assertThat(json.readTree(json.writeValueAsString(Error.validation("Name is required"))))
                .isEqualTo(json.readTree("{\"code\":\"Validation.Error\",\"description\":\"Name is required\",\"statusCode\":422}"));
    }

    @Test
    void aResultWithAValueHasTheSameFields() throws Exception {
        assertThat(json.readTree(json.writeValueAsString(Result.success(7))))
                .isEqualTo(json.readTree("{\"value\":7,\"isSuccess\":true,\"isFailure\":false,\"error\":{\"code\":\"\",\"description\":\"\",\"statusCode\":200}}"));
    }

    @Test
    void readingTheValueOfAFailureIsAnError() {
        ValueResult<String> failed = Result.failure(Error.conflict("Taken"), String.class);
        assertThat(failed.isFailure()).isTrue();
        assertThatThrownBy(failed::getValue).hasMessage("Cannot access Value of a failure result. Error: Taken");
        assertThat(Result.create(null).getError()).isEqualTo(Error.NULL_VALUE);
    }
}
