package Trans.demo;

import java.util.ArrayList;

class Assign {
    private int _column;
    private int _line;
    private VarNode _def;
    private ArrayList<VarNode> _use = new ArrayList<>();

    Assign(Stmt stmt, int line, int column) {
        _line = line;
        _column = column;
        stmt.addAssign(this);
    }

    void setDef(VarNode def) {
        _def = def;
    }

    VarNode getDef() {
        return _def;
    }

    void addUse(VarNode use) {
        _use.add(use);
    }

    ArrayList<VarNode> getUse() {
        return _use;
    }

    int getLine() {
        return _line;
    }

    int getColumn() {
        return  _column;
    }
}
