package tagtable;

public class Tag {
    private TagPriority priority;
    private String containment;
    Tag(TagPriority priority, String value, int size) {
        this.priority = priority;
        this.containment = value;
    }

    public TagPriority getPriority() {
        return priority;
    }
    public String getValue() {
        return containment;
    }

    @Override
    public String toString() {
        return containment + "-" + priority.name();
    }
    @Override
    public int hashCode() {
        return containment.hashCode() + priority.name().hashCode();
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Tag && (
                (containment.equals(((Tag) o).containment) && priority.equals(((Tag) o).priority))
                //Check for substituted tags
        );
    }
}
