package SyntacticAnalyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Analyzer {
    private static final String CONST = "CONST";
    private static final String AND = "AND";
    private static final String OR = "OR";
    private static final String NOT = "NOT";
    private static final String ADD = "ADD";
    private static final String SUB = "SUB";
    private static final String DIV = "DIV";
    private static final String MUL = "MUL";
    private static final String MOL = "MOL";
    private static final String NUM = "num";
    private static final String INT = "INT";
    private static final String ASSIGNOP = "ASSIGNOP";
    private static final String VOID = "VOID";
    private static final String MAIN = "MAIN";
    public static String Ident = "identifier";
    public static String bound_LP = "bound_LP";
    public static String bound_RP = "bound_RP";
    public static String bound_SEMI = "bound_SEMI";
    private static final String COMMA = "COMMA";
    private static final String bound_LC = "bound_LC";
    private static final String bound_RC = "bound_RC";
    private final String relOperator = "relOperator";
    private String Array = "Array";

    private final List<String> tokenLines = new ArrayList<>();//辅助文件读入的列表
    private List<Token> tokenList = new ArrayList<>();//存放token的列表
    private List<ASTNode> leaves = new ArrayList<>();//用于存放抽象语法树的叶子节点
    private boolean flag = true;//标志是否能够生成抽象语法树
    private int tokenPointer = 0;
    private String FILENAME = "C:\\Users\\96392\\Desktop\\实验报告\\编译技术2\\语法分析器\\词法分析器\\result.txt";


    public Analyzer() {
        //按行读取键值对
        try {
            FileReader fileReader = new FileReader(FILENAME);
            BufferedReader bf = new BufferedReader(fileReader);
            String str;
            while ((str = bf.readLine()) != null) {
                tokenLines.add(str);
            }
            bf.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在！");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //检验是否完全输入
        for (String str : tokenLines) {
            Token token = new Token(str.split("\t", 5)[0], str.split("\t", 5)[3]);
            tokenList.add(token);
        }
    }

    //获取token类型
    public String getTokenType(int position) {
        return tokenList.get(position).getType();
    }

    public String getTokenType() {
        return tokenList.get(tokenPointer).getType();
    }

    //显示所有键值对
    public void displayTokens() {
        for (Token token : tokenList) {
            System.out.println("<" + token.getType() + "," + token.getValue() + ">");
        }
    }

    public void mainLoop() {
        //初始化指针
        tokenPointer = 0;
        CompUnit(0);
    }

    public void CompUnit(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "CompUnit"));
        String str = tokenList.get(tokenPointer).getType();

        //连续读入两个token来确定进入哪个分支
        while ((str.equals(INT) || str.equals(VOID) || str.equals(CONST)) && flag && tokenPointer < tokenList.size()) {
            if (str.equals(VOID))
                FuncDef(level);
            else if (str.equals(INT)) {
                if ((getTokenType(tokenPointer + 1).equals(Ident) || getTokenType(tokenPointer + 1).equals(MAIN)) && getTokenType(tokenPointer + 2).equals(bound_LP))
                    FuncDef(level);
                else if (getTokenType(tokenPointer + 1).equals(Ident))
                    Decl(level);
            } else if (str.equals(CONST))
                Decl(level);
            if (tokenPointer >= tokenList.size() || !flag)
                return;
            str = getTokenType();
        }

    }

    private void Decl(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "Decl"));
        String str = tokenList.get(tokenPointer).getType();

        if (str.equals(CONST))
            ConstDecl(level);
        else if (str.equals(INT))
            VarDecl(level);
    }

    private void ConstDecl(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "ConstDecl"));

        readWord(level, CONST);
        BType(level);
        ConstDef(level);
        String str = tokenList.get(tokenPointer).getType();
        while (!str.equals(bound_SEMI) && flag && tokenPointer < tokenList.size()) {
            readWord(level, COMMA);
            ConstDef(level);
            if (tokenPointer >= tokenList.size() || !flag)
                return;
            str = tokenList.get(tokenPointer).getType();
        }
        readWord(level, bound_SEMI);
    }

    private void ConstDef(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "ConstDef"));

        //这里有修改，可直接识别数组类型
        String str = tokenList.get(tokenPointer).getType();
        if (str.equals(Ident))
            readWord(level, Ident);
        else
            readWord(level, Array);
        readWord(level, ASSIGNOP);
        ConstInitVal(level);
    }

    private void ConstInitVal(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "ConstInitVal"));
        String str = getTokenType();
        if (str.equals(bound_LC)) {
            readWord(level, bound_LC);

            str = getTokenType();
            if (str.equals(bound_RC)) {
                readWord(level, bound_RC);
            } else {
                ConstInitVal(level);
                str = getTokenType();
                while (str.equals(COMMA) && flag) {
                    readWord(level, COMMA);
                    ConstInitVal(level);
                    str = getTokenType();
                }
                readWord(level, bound_RC);
            }
        } else
            ConstExp(level);
    }

    private void ConstExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "ConstExp"));

        AddExp(level);
    }

    public void FuncDef(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "FuncDef"));
        FuncType(level);
        //要考虑main函数
        String str = tokenList.get(tokenPointer).getType();
        if (str.equals((Ident)))
            readWord(level, Ident);
        else
            readWord(level, MAIN);

        readWord(level, "bound_LP");

        str = tokenList.get(tokenPointer).getType();
        if (!str.equals(bound_RP))
            FuncFParams(level);
        readWord(level, "bound_RP");
        Block(level);
    }

    private void FuncFParams(int level) {
        leaves.add(new ASTNode(level++, "FuncParams"));
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        FuncFParam(level);
        while (tokenList.get(tokenPointer).getType().equals(COMMA)) {
            readWord(level, COMMA);
            FuncFParam(level);
        }
    }

    private void FuncFParam(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;
        if (tokenList.get(tokenPointer).getType().equals(bound_RP))
            return;

        leaves.add(new ASTNode(level++, "FuncParam"));
        readWord(level, "INT");

        String str = getTokenType();
        if (str.equals(Ident))
            readWord(level, Ident);
        else
            readWord(level, Array);
        readWord(level, bound_RP);

    }

    private void FuncType(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;
        leaves.add(new ASTNode(level++, "FuncType"));

        String str = tokenList.get(tokenPointer++).getType();
        if (str.equals("INT"))
            leaves.add(new ASTNode(level, str));
        else if (str.equals("VOID"))
            leaves.add(new ASTNode(level, str));
        else {
            flag = false;
            return;
        }
    }

    private void Block(int level) {//Block
        if (tokenPointer >= tokenList.size())
            return;
        leaves.add(new ASTNode(level++, "Block"));
        readWord(level, bound_LC);

        while (!tokenList.get(tokenPointer).getType().equals("bound_RC") && flag && tokenPointer < tokenList.size()) {
            BlockItem(level);
        }

        readWord(level, bound_RC);
    }

    private void BlockItem(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;
        String str = tokenList.get(tokenPointer).getType();
        leaves.add(new ASTNode(level++, "BlockItem"));

        if (str.equals("INT")) {
            VarDecl(level);
        } else if (str.equals("IF") || str.equals("WHILE") || str.equals("BREAK") || str.equals("RETURN") || str.equals(Ident)) {
            Stmt(level);
        } else {
            flag = false;
            return;
        }
    }

    private void Stmt(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;
        leaves.add(new ASTNode(level++, "Stmt"));


        String str = tokenList.get(tokenPointer).getType();
        if (str.equals("IF")) {
            readWord(level, "IF");
            readWord(level, bound_LP);
            cond(level);
            readWord(level, bound_RP);
            Stmt(level);

            //判断有误else
            if (tokenPointer >= tokenList.size() || !flag)
                return;
            str = tokenList.get(tokenPointer).getType();
            if (str.equals("ELSE")) {
                readWord(level, "ELSE");
                Stmt(level);
            }
        } else if (str.equals("WHILE")) {
            readWord(level, "WHILE");
            readWord(level, "bound_LP");
            cond(level);
            readWord(level, "bound_RP");
            Stmt(level);

        } else if (str.equals("RETURN")) {
            readWord(level, "RETURN");
            if (!tokenList.get(tokenPointer).getType().equals(bound_SEMI)) {
                Exp(level);
            }
            readWord(level, "bound_SEMI");
        } else if (str.equals("BREAK")) {
            readWord(level, "BREAK");
            readWord(level, bound_SEMI);
        } else if (str.equals("CONTINUE")) {
            readWord(level, "CONTINUE");
            readWord(level, bound_SEMI);
        } else if (str.equals(bound_LC)) {
            Block(level);
        } else if (str.equals(bound_SEMI)) {
            readWord(level, bound_SEMI);
        } else if (str.equals(Ident)) {
            readWord(level, Ident);
            readWord(level, ASSIGNOP);
            Exp(level);
            readWord(level, bound_SEMI);
        } else if (str.equals(Array)) {
            readWord(level, Array);
            readWord(level, ASSIGNOP);
            Exp(level);
            readWord(level, bound_SEMI);

        } else {
            flag = false;
        }
    }

    private void Exp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;
        leaves.add(new ASTNode(level++, "Exp"));

        AddExp(level);
    }

    private void cond(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "Cond"));

        LOrExp(level);
    }

    private void LOrExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "LOrExp"));

        LAndExp(level);
        LOrExp_(level);
    }

    private void LOrExp_(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        if (leaves.get(tokenPointer).getType().equals(OR)) {
            readWord(level, OR);
            LAndExp(level);
        }
    }

    private void LAndExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "LAndExp"));

        EqExp(level);
        LAndExp_(level);
    }

    private void LAndExp_(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        if (leaves.get(tokenPointer).getType().equals(AND)) {
            readWord(level, AND);
            EqExp(level);
            LAndExp_(level);
        }
    }

    private void EqExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "EqExp"));

        RelExp(level);
        EqExp_(level);
    }

    private void EqExp_(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        if (tokenList.get(tokenPointer).getValue().equals("==") || tokenList.get(tokenPointer).getValue().equals("!=")) {
            readWord(level, relOperator);
            RelExp(level);
            EqExp_(level);
        }
    }

    private void RelExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "RelExp"));

        AddExp(level);
        RelExp_(level);
    }

    private void RelExp_(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        if (tokenList.get(tokenPointer).getType().equals(relOperator)) {
            readWord(level, relOperator);
            AddExp(level);
            RelExp_(level);
        }
    }

    private void AddExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "AddExp"));

        MulExp(level);
        AddExp_(level);
    }

    private void AddExp_(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        String str = tokenList.get(tokenPointer).getType();
        if (str.equals(ADD) || str.equals(SUB)) {
            readWord(level, str);
            MulExp(level);
            AddExp_(level);
        }
    }

    private void MulExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "MulExp"));

        UnaryExp(level);
        MulExp_(level);
    }

    private void MulExp_(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        String str = tokenList.get(tokenPointer).getType();
        if (str.equals(DIV) || str.equals(MUL) || str.equals(MOL)) {
            readWord(level, str);
            UnaryExp(level);
            MulExp_(level);
        }
    }

    private void UnaryExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "UnaryExp"));
        String str = tokenList.get(tokenPointer).getType();

        if (str.equals(ADD) || str.equals(SUB) || str.equals(NOT)) {
            UnaryOp(level);
            UnaryExp(level);
        } else if (str.equals(Ident)) {
            if (tokenList.get(tokenPointer + 1).getType().equals(bound_LP)) {
                readWord(level, Ident);
                readWord(level, bound_LP);
                FuncFParam(level);
                readWord(level, bound_RP);
            } else {
                PrimaryExp(level);
            }
        } else if (str.equals(bound_LP) || str.equals(NUM) || str.equals(Array)) {
            PrimaryExp(level);
        } else
            flag = false;
    }

    private void PrimaryExp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "PrimaryExp"));
        String str = tokenList.get(tokenPointer).getType();

        if (str.equals(bound_LP)) {
            readWord(level, bound_LP);
            Exp(level);
            readWord(level, bound_RP);
        } else if (str.equals(Ident) || str.equals(Array)) {
//            if (tokenList.get(tokenPointer + 1).getType().equals(bound_LP))
//                Func();
//            else
            LVal(level);
        } else if (str.equals(NUM)) {
            readWord(level, NUM);
        } else
            flag = false;
    }

    private void LVal(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;
        leaves.add(new ASTNode(level++, "LVal"));
        String str = getTokenType();
        if (str.equals(Ident))
            readWord(level, Ident);
        else
            readWord(level, Array);
    }

    private void UnaryOp(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "UnaryOp"));
        String str = leaves.get(tokenPointer).getType();

        if (str.equals(ADD)) {
            readWord(level, ADD);
        } else if (str.equals(SUB)) {
            readWord(level, SUB);
        } else if (str.equals(NOT)) {
            readWord(level, NOT);
        } else
            flag = false;
    }

    public void VarDecl(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "VarDecl"));

        BType(level);
        VarDef(level);
        String str = tokenList.get(tokenPointer).getType();
        while (!str.equals(bound_SEMI) && flag && tokenPointer < tokenList.size()) {
            readWord(level, COMMA);
            VarDef(level);
        }
        readWord(level, bound_SEMI);
    }

    private void VarDef(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;
        leaves.add(new ASTNode(level++, "VarDef"));

        String str = getTokenType();

        if (str.equals(Array))
            readWord(level, Array);
        else
            readWord(level, Ident);

        str = getTokenType();
        if (str.equals(ASSIGNOP)) {
            readWord(level, ASSIGNOP);
            initVal(level);
        }
    }

    private void BType(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "BType"));
        readWord(level, INT);
    }

    private void initVal(int level) {
        if (tokenPointer >= tokenList.size() || !flag)
            return;

        leaves.add(new ASTNode(level++, "initVal"));
        String str = getTokenType();

        if (str.equals(bound_LC)) {
            readWord(level, bound_LC);
            initVal(level);
            str = getTokenType();
            while (str.equals(COMMA) && flag) {
                readWord(level, COMMA);
                initVal(level);
                str = getTokenType();
            }
            readWord(level, bound_RC);
        } else {
            Exp(level);
        }
    }

    //显示抽象树
    public void displayAST() {
        if (!flag)
            System.out.println("抽象语法树生成失败！");
        else
            System.out.println("抽象语法树生成成功！");
        for (ASTNode node : leaves) {
            String str = "";
            for (int i = 0; i < node.getLevel(); i++)
                str += "\t";
            str += "|-" + node.getType();
            System.out.println(str);
        }
    }

    //用于简便识别指定token
    public boolean readWord(int level, String word) {
        if (tokenPointer >= tokenList.size() || !flag)
            return false;

        String str = tokenList.get(tokenPointer++).getType();

        if (str.equals(word)) {
            leaves.add(new ASTNode(level, str));
            return true;
        } else {
            flag = false;
            return false;
        }
    }

    //AST强制复位
    public void resetLeaves(int position) {
        for (int i = position; i < leaves.size(); i++)
            leaves.remove(i);
    }

    public static void main(String[] args) {
        Analyzer analyzer = new Analyzer();
        analyzer.displayTokens();
        analyzer.mainLoop();
        analyzer.displayAST();
    }
}
