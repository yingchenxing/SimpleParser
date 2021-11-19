package SyntacticAnalyzer;

public class ASTNode {
    private int level;
    private String type;
    private String value = null;

    public ASTNode(int level, String type, String value) {
        this.level = level;
        this.type = type;
        this.value = value;
    }

    public ASTNode(int level, String type) {
        this.level = level;
        this.type = type;
        this.value = null;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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
