package Room1.gameRoom1Logic.sfs2x;

import java.util.HashMap;

public class Game1 {

    protected int[] probList = {88, 96, 104};					//機率清單
    protected int maxGrid = 1;									//格
    protected int maxReel = 3;									//軸
    protected int maxLine = 1;									//線
    protected int betTimes = 1;								//一次押幾線

    protected int freeCount = 10;			//免費遊戲次數
    protected int superCount = 10;

    protected int superToSuperCount = 5;	//超級免費次數
    protected int freeToFreeCount = 5;		//免費中遊戲次數

    protected int[] superTimesRange = {1, 2, 3};
    protected int[] superTimesWeight = {40, 30, 30};

    @SuppressWarnings("serial")
    protected HashMap<String, Integer> gameGrid = new HashMap<String, Integer>()
    {{put("0", 0);
        put("1", 1);
        put("2", 2);
        put("3", 3);
        put("4", 4);
        put("5", 5);
        put("6", 6);
        put("7", 7);
//	  put("Wild", 9);
        put("FreeSpin", 8);}};


    protected int[] payTable =
            {
                    10,		//0
                    20,		//1
                    30,		//2
                    50,		//3
                    80,		//4
                    150,	//5
                    400,	//6
                    800,	//7
                    0,		//free
            };


    protected int[][] baseWheel_88 =
            //RTP:88 BASE:48 FREE:40  平均125局出一次免費遊戲
            {
                    {0,1,2,3,4,5,6,7,8},
                    {0,1,2,3,4,5,6,7,8},
                    {0,1,2,3,4,5,6,7,8},
            };

    protected int[][] baseWheel_96 =
            //RTP:96 BASE:56 FREE:40  平均125局出一次免費遊戲
            {
                    {0,1,2,3,4,5,6,7,8},
                    {0,1,2,3,4,5,6,7,8},
                    {0,1,2,3,4,5,6,7,8},
            };

    protected int[][] baseWheel_104 =
            //RTP:104 BASE:64 FREE:40  平均125局出一次免費遊戲
            {
                    {0,1,2,3,4,5,6,7,8},
                    {0,1,2,3,4,5,6,7,8},
                    {0,1,2,3,4,5,6,7,8},
            };

    protected int[][] freeWheel =
            {
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6,7,7,7},
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,2,2,2,2,3,3,3,4,4,4,4,8},
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,2,2,2,2,3,3,3,4,4,4,4,8},
            };

    protected int[][] superWheel =
            {
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6,7,7,7},
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,2,2,2,2,3,3,3,4,4,4,4,8},
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,2,2,2,2,3,3,3,4,4,4,4,8},
            };


}
