package fr.traqueur.cachelink.serialization;

public interface Serializer<T> {

    StringSerializer STRING = new StringSerializer();
    IntegerSerializer INTEGER = new IntegerSerializer();
    LongSerializer LONG = new LongSerializer();
    DoubleSerializer DOUBLE = new DoubleSerializer();
    FloatSerializer FLOAT = new FloatSerializer();
    BooleanSerializer BOOLEAN = new BooleanSerializer();
    ByteSerializer BYTE = new ByteSerializer();
    ShortSerializer SHORT = new ShortSerializer();
    CharacterSerializer CHARACTER = new CharacterSerializer();
    ByteArraySerializer BYTE_ARRAY = new ByteArraySerializer();

    String serialize(T object);

    T deserialize(String object);

    class StringSerializer implements Serializer<String> {
        @Override
        public String serialize(String object) {
            return object;
        }

        @Override
        public String deserialize(String object) {
            return object;
        }
    }

    class IntegerSerializer implements Serializer<Integer> {
        @Override
        public String serialize(Integer object) {
            return object.toString();
        }

        @Override
        public Integer deserialize(String object) {
            return Integer.parseInt(object);
        }
    }

    class LongSerializer implements Serializer<Long> {
        @Override
        public String serialize(Long object) {
            return object.toString();
        }

        @Override
        public Long deserialize(String object) {
            return Long.parseLong(object);
        }
    }

    class DoubleSerializer implements Serializer<Double> {
        @Override
        public String serialize(Double object) {
            return object.toString();
        }

        @Override
        public Double deserialize(String object) {
            return Double.parseDouble(object);
        }
    }

    class FloatSerializer implements Serializer<Float> {
        @Override
        public String serialize(Float object) {
            return object.toString();
        }

        @Override
        public Float deserialize(String object) {
            return Float.parseFloat(object);
        }
    }

    class BooleanSerializer implements Serializer<Boolean> {
        @Override
        public String serialize(Boolean object) {
            return object.toString();
        }

        @Override
        public Boolean deserialize(String object) {
            return Boolean.parseBoolean(object);
        }
    }

    class ByteSerializer implements Serializer<Byte> {
        @Override
        public String serialize(Byte object) {
            return object.toString();
        }

        @Override
        public Byte deserialize(String object) {
            return Byte.parseByte(object);
        }
    }

    class ShortSerializer implements Serializer<Short> {
        @Override
        public String serialize(Short object) {
            return object.toString();
        }

        @Override
        public Short deserialize(String object) {
            return Short.parseShort(object);
        }
    }

    class CharacterSerializer implements Serializer<Character> {
        @Override
        public String serialize(Character object) {
            return object.toString();
        }

        @Override
        public Character deserialize(String object) {
            return object.charAt(0);
        }
    }

    class ByteArraySerializer implements Serializer<byte[]> {
        @Override
        public String serialize(byte[] object) {
            return new String(object);
        }

        @Override
        public byte[] deserialize(String object) {
            return object.getBytes();
        }
    }

}
