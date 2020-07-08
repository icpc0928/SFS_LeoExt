package Room1.gameRoom1Logic.sfs2x;

public enum  GameState {
    STATE_BASE_GAME(0),
    STATE_FREE_GAME(1),
    STATE_SUPER_GAME(2);


    GameState(int id ){
        this.id = id;
    }
    private int id;
    public int getId(){
        return id;
    }

}


