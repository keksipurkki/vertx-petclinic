package net.keksipurkki.petstore.support;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import io.vertx.core.Future;

import java.util.Iterator;
import java.util.Optional;

/**
 * Patch OpenAPI specification generator to play nice with Vert.x Futures
 * <p>
 * Whenever an API operation produces a Future<T>, the converter
 * extracts T and maps it to a schema under '#/components/schemas/T'
 */
public class FutureModelConverter implements ModelConverter {

    private AnnotatedType futureValue(AnnotatedType type) {
        JavaType _type = Json.mapper().constructType(type.getType());

        if (_type == null) {
            return type;
        }

        if (!Future.class.isAssignableFrom(_type.getRawClass())) {
            return type;
        }

        var valueType = _type.findTypeParameters(_type.getRawClass())[0];
        return new AnnotatedType().type(valueType).resolveAsRef(true);
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        return Optional.ofNullable(chain.next())
            .map(c -> c.resolve(futureValue(type), context, chain))
            .orElse(null);
    }
}
