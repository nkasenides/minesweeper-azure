package model;

public class CellState {

    private boolean mined;
    private RevealState revealState;

    public CellState() { }

    public CellState(boolean mined) {
        this.mined = mined;
        this.revealState = RevealState.COVERED;
    }

    public boolean isMined() {
        return mined;
    }

    public RevealState getRevealState() {
        return revealState;
    }

    public void setMined(boolean isMined) {
        this.mined = isMined;
    }

    public void setRevealState(RevealState revealState) {
        this.revealState = revealState;
    }

}
