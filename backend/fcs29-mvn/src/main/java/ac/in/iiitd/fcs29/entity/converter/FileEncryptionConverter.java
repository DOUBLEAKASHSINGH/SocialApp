package ac.in.iiitd.fcs29.entity.converter;

import ac.in.iiitd.fcs29.configuration.util.EncryptUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FileEncryptionConverter implements AttributeConverter<byte[], byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(byte[] attribute) {
        return (attribute == null) ? null : EncryptUtil.encrypt(attribute);
    }

    @Override
    public byte[] convertToEntityAttribute(byte[] dbData) {
        return (dbData == null) ? null : EncryptUtil.decrypt(dbData);
    }
}
