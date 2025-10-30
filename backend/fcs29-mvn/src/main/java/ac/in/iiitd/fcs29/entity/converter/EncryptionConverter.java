package ac.in.iiitd.fcs29.entity.converter;

import ac.in.iiitd.fcs29.configuration.util.EncryptUtil;
import jakarta.persistence.Converter;

import jakarta.persistence.AttributeConverter;

@Converter
public class EncryptionConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return (attribute == null) ? null : EncryptUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return (dbData == null) ? null : EncryptUtil.decrypt(dbData);
    }
}
