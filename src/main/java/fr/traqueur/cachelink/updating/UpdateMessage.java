package fr.traqueur.cachelink.updating;

public class UpdateMessage {

    private final String cacheId;
        private final String cacheName;
        private final Operation operation;
        private final String key;
        private final String value;

        public UpdateMessage(String cacheId, String cacheName, Operation operation, String key, String value) {
            this.cacheId = cacheId;
            this.cacheName = cacheName;
            this.operation = operation;
            this.key = key;
            this.value = value;
        }

        public String cacheId() {
            return cacheId;
        }

        public String cacheName() {
            return cacheName;
        }

        public Operation operation() {
            return operation;
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }
    }