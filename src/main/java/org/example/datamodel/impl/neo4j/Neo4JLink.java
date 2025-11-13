package org.example.datamodel.impl.neo4j;

public record Neo4JLink(
        String label,
        String node1,
        String node2,
        String node1Label,
        String node2Label,
        String node1Prop,
        String node2Prop
) {
    public static class Builder {
            private String _label;
        private String _node1;
        private String _node2;
        private String _node1Label;
        private String _node2Label;
        private String _node1Prop;
        private String _node2Prop;

        public static Builder create() {
            return new Builder();
        }

        public Builder withLabel(String label) {
            _label = label;
            return this;
        }

        public Builder betweenLabels(String label1, String label2) {
            _node1Label = label1;
            _node2Label = label2;

            return this;
        }

        public Builder betweenLabels(String label) {
            _node1Label = label;
            _node2Label = label;

            return this;
        }

        public Builder betweenProps(String prop1, String prop2) {
            _node1Prop = prop1;
            _node2Prop = prop2;

            return this;
        }

        public Builder betweenProps(String prop) {
            _node1Prop = prop;
            _node2Prop = prop;

            return this;
        }

        public Builder parentValue(String node1) {
            _node1 = node1;
            return this;
        }

        public Builder childValue(String node2) {
            _node2 = node2;
            return this;
        }

        public Builder parentLabel(String node1Label) {
            _node1Label = node1Label;
            return this;
        }

        public Builder childLabel(String node2Label) {
            _node2Label = node2Label;
            return this;
        }

        public Builder parentProp(String node1Prop) {
            _node1Prop = node1Prop;
            return this;
        }

        public Builder childProp(String node2Prop) {
            _node2Prop = node2Prop;
            return this;
        }

        public Neo4JLink build() {
            if (_label == null) {
                throw new IllegalStateException("Label must be set");
            }
            if (_node1 == null) {
                throw new IllegalStateException("First node must be set");
            }
            if (_node2 == null) {
                throw new IllegalStateException("Second node must be set");
            }
            if (_node1Label == null) {
                throw new IllegalStateException("First node label must be set");
            }
            if (_node2Label == null) {
                throw new IllegalStateException("Second node label must be set");
            }
            if (_node1Prop == null) {
                throw new IllegalStateException("First node property must be set");
            }
            if (_node2Prop == null) {
                throw new IllegalStateException("Second node property must be set");
            }

            return new Neo4JLink(_label, _node1, _node2, _node1Label, _node2Label, _node1Prop, _node2Prop);
        } }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(label, node1, node2, node1Label, node2Label, node1Prop, node2Prop);
    }
}
