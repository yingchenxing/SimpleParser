package SyntacticAnalyzer;

public class ASTNode {
    private int weight;
    private String type;
    private String value = null;

    public ASTNode(int weight, String type, String value) {
        this.weight = weight;
        this.type = type;
        this.value = value;
    }

    public ASTNode(int weight, String type) {
        this.weight = weight;
        this.type = type;
        this.value = null;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
