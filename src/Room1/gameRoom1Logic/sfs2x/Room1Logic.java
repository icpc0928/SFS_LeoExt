package Room1.gameRoom1Logic.sfs2x;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Room1Logic {

    private int gameID;
    private int maxGrid;
    private int maxReel;
    private int maxLine;
    private int betTimes;

    private int useProb = 0;
    private int[] probList;

    private int freeCount;
    private int superCount;

    private int	superToSuperCount;
    private int	freeToFreeCount;

    private int[] superTimesRange;
    private int[] superTimesWeight;

    private int[] payTable;
    private int[][] baseWheel_88;
    private int[][] baseWheel_96;
    private int[][] baseWheel_104;
    private int[][] freeWheel;
    private int[][] superWheel;

    private boolean testMode;
    private int[][] testBaseWheel;
    private int[][] testFreeWheel;
    private int[][] testSuperWheel;

    private HashMap<String, Integer> gameGrid;

    private Game1 game1 = null;

    public Room1Logic(int gameID){
        this.testMode = false;
        this.gameID = gameID;

        switch (gameID){
            default: return;

            case 1 :
                game1 = new Game1();
                this.maxReel = game1.maxReel;
                this.maxGrid = game1.maxGrid;
                this.maxLine = game1.maxLine;
                this.betTimes = game1.betTimes;
                this.gameGrid = game1.gameGrid;
                this.payTable = game1.payTable;
                this.freeCount = game1.freeCount;
                this.superCount = game1.superCount;
                this.freeToFreeCount = game1.freeToFreeCount;
                this.superToSuperCount = game1.superToSuperCount;
                this.superWheel = game1.superWheel;
                this.freeWheel = game1.freeWheel;
                this.baseWheel_88 = game1.baseWheel_88;
                this.baseWheel_96 = game1.baseWheel_96;
                this.baseWheel_104 = game1.baseWheel_104;
                this.probList = game1.probList;
                this.superTimesRange = game1.superTimesRange;
                this.superTimesWeight = game1.superTimesWeight;
                break;
        }
    }
    //開桌 初始化圖示
    public int[] initGrid(double prob)
    {
        if(testMode == true)
        {
            this.testBaseWheel = loadTestProbXls(0);
            this.testFreeWheel = loadTestProbXls(1);
            this.testSuperWheel = loadTestProbXls(2);
        }

        return setBaseGrid(prob, prob);
    }
    ///////////////////////////////////////////////
    //出結果
    //////////////////////////////////////////////

    //主遊戲結果
    public HashMap<String, Object> getBaseResult(long lineBet, double rate, double prob, int[] freeLight)
    {
        HashMap<String, Object> resp = new HashMap<String, Object>();
        int[] grid = new int[maxReel];

        //出牌
        grid = setBaseGrid(rate, prob);

        //算獎
        HashMap<String, Object> awardMap = getAward(lineBet, grid, freeLight);

        resp.put("Grid", grid);
        resp.put("Award", (int) awardMap.get("Award"));
        resp.put("TotalWin", (long) awardMap.get("Win"));
        resp.put("FreeMode", (int) awardMap.get("FreeMode"));
        resp.put("FreeLight", (int[]) awardMap.get("FreeLight"));
        resp.put("FreeSpinCount", (int) awardMap.get("FreeSpinCount"));
        resp.put("SuperSpinCount", (int) awardMap.get("SuperSpinCount"));
        resp.put("UseProb", useProb);

        return resp;
    }


    //免費遊戲結果
    public HashMap<String, Object> getFreeResult(long lineBet, double rate, double prob, int[] freeLight)
    {
        HashMap<String, Object> resp = new HashMap<String, Object>();

        int[] grid = new int[maxReel];

        //出牌
        grid = setFreeGrid(rate, prob);

        //算獎
        HashMap<String, Object> awardMap = getAward(lineBet, grid, freeLight);

        resp.put("Grid", grid);
        resp.put("Award", (int) awardMap.get("Award"));
        resp.put("TotalWin", (long) awardMap.get("Win"));
        resp.put("FreeMode", (int) awardMap.get("FreeMode"));
        resp.put("FreeLight", (int[]) awardMap.get("FreeLight"));
        resp.put("FreeSpinCount", (int) awardMap.get("FreeSpinCount"));
        resp.put("SuperSpinCount", (int) awardMap.get("SuperSpinCount"));

        return resp;
    }


    public HashMap<String, Object> getSuperResult(long lineBet, double rate, double prob)
    {
        HashMap<String, Object> resp = new HashMap<String, Object>();

        int[] grid = new int[maxReel];

        //出牌
        grid = setSuperGrid(rate, prob);

        //算獎
        HashMap<String, Object> awardMap = getSuperAward(lineBet, grid);

        resp.put("Grid", grid);
        resp.put("Award", (int) awardMap.get("Award"));
        resp.put("Times", (long) awardMap.get("Times"));
        resp.put("TotalWin", (long) awardMap.get("Win"));
        resp.put("SuperSpinCount", (int) awardMap.get("SuperSpinCount"));

        return resp;
    }


    ///////////////////////////////////////////////
    //出牌
    //////////////////////////////////////////////

    //主遊戲出牌
    private int[] setBaseGrid(double rate, double prob)
    {
        int[]grid = new int[maxReel];

        int[][]wheel = getProbWheel(GameState.STATE_BASE_GAME.getId(), rate, prob);

        for(int i = 0; i < maxReel; i++)
        {
            int rand = (int)(Math.random() * wheel[i].length);

            int index = rand;
            if(index >= wheel[i].length)
                index -= wheel[i].length;

            grid[i] = wheel[i][index];
        }

        return grid;
    }

    //免費遊戲出牌
    private int[] setFreeGrid(double rate, double prob)
    {
        int[]grid = new int[maxReel];

        int[][]wheel = getProbWheel(GameState.STATE_FREE_GAME.getId(), rate, prob);

        for(int i = 0; i < maxReel; i++)
        {
            int rand = (int)(Math.random() * wheel[i].length);

            int index = rand;
            if(index >= wheel[i].length)
                index -= wheel[i].length;

            grid[i] = wheel[i][index];
        }

        return grid;
    }

    //超級遊戲出牌
    private int[] setSuperGrid(double rate, double prob)
    {
        int[] grid = new int[maxReel];

        int[][] wheel = getProbWheel(GameState.STATE_SUPER_GAME.getId(), rate, prob);

        for(int i = 0; i < maxReel; i++)
        {
            int rand = (int)(Math.random() * wheel[i].length);

            int index = rand;
            if(index >= wheel[i].length)
                index -= wheel[i].length;

            grid[i] = wheel[i][index];
        }

        return grid;
    }

    //機率平衡
    private int[][] getProbWheel(int gameState, double rate, double prob)
    {
        int[] range = new int[probList.length];
        System.arraycopy(probList, 0, range, 0, probList.length);
        int gap = 0;
        int target = 0;
        int[][] wheel = null;

        for(int i = 0; i < range.length; i++)
        {
            if(prob >= range[i])
                target = i;
        }

        gap = (int) ((prob - rate) / 2);

        if(prob > rate)
        {
            target += (gap + 1);

            if(target > range.length - 1)
                target = range.length - 1;
        }
        else if(prob < rate)
        {
            target += (gap - 1);

            if(target < 0)
                target = 0;
        }

        useProb = range[target];

        if(gameState == GameState.STATE_FREE_GAME.getId())
            useProb = 0;
        else if(gameState == GameState.STATE_SUPER_GAME.getId())
            useProb = 1;

        switch(useProb)
        {
            //免費遊戲
            case 0:
                wheel = freeWheel;
                break;
            case 1:
                wheel = superWheel;
                break;
            case 88:
                wheel = baseWheel_88;
                break;
            case 96:
                wheel = baseWheel_96;
                break;
            case 104:
                wheel = baseWheel_104;
                break;
        }


        ///////////////測試機率////////////////////
        if(testMode == true)
        {
            if(gameState == GameState.STATE_BASE_GAME.getId())
                wheel = this.testBaseWheel;
            else if(gameState == GameState.STATE_FREE_GAME.getId())
                wheel = this.testFreeWheel;
            else if(gameState == GameState.STATE_SUPER_GAME.getId())
                wheel = this.testSuperWheel;
        }

        return wheel;
    }

    ////////////////////////////////////////////
    //算獎
    ////////////////////////////////////////////

    //算獎
    private HashMap<String, Object> getAward(long lineBet, int[] grid, int[] freeLight)
    {
        HashMap<String, Object> resp = new HashMap<String, Object>();

        int award = 99;
        int awardPayTable = 0;

        //算連線
        int count = 0;
        for(int i = 0; i < payTable.length; i++)
        {
            count = 0;

            for(int j = 0; j < grid.length; j++)
            {
                if(grid[j] == i)
                    count++;
            }

            if(count == 3)
            {
                if(payTable[i] > awardPayTable)
                {
                    award = i;
                    awardPayTable = payTable[i];
                }
            }
        }

        //贏分
        long win = 0;
        win = awardPayTable * lineBet;

        //算免費圖標數
        int[] light = Arrays.copyOf(freeLight, freeLight.length);
        int freeMode = 0;
        int freeShowCount = 0;
        for(int i = 0; i < this.maxReel; i++)
        {
            if(grid[i] == gameGrid.get("FreeSpin"))
            {
                light[i] = 1;
                freeShowCount++;
            }
        }

        //算燈數
        int lightCount = 0;
        for(int i : light)
        {
            if(i == 1)
                lightCount++;
        }

        //算免費模式
        if(lightCount == this.maxReel)
            freeMode = freeShowCount == this.maxReel ? 2 : 1;

        resp.put("Award", award);
        resp.put("PayTable", awardPayTable);
        resp.put("Win", win);
        resp.put("FreeLight", light);
        resp.put("FreeMode", freeMode);
        resp.put("FreeSpinCount", freeMode == 1 ? freeCount : 0);
        resp.put("SuperSpinCount", freeMode == 2 ? superCount : 0);
        resp.put("FreeShowCount", freeShowCount);

        return resp;
    }

    //算超級遊戲獎
    private HashMap<String, Object> getSuperAward(long lineBet, int[] grid)
    {
        HashMap<String, Object> resp = new HashMap<String, Object>();

        int award = 99;
        int awardPayTable = 0;

        //取倍率
        int times = getSuperTimes();

        //算連線
        int count = 0;
        for(int i = 0; i < payTable.length; i++)
        {
            count = 0;

            for(int j = 0; j < grid.length; j++)
            {
                if(grid[j] == i)
                    count++;
            }

            if(count == 3)
            {
                if(payTable[i] > awardPayTable)
                {
                    award = i;
                    awardPayTable = payTable[i];
                }
            }
        }

        //贏分
        long win = 0;
        win = awardPayTable * lineBet * times;

        //算圖標出現次數
        int freeShowCount = 0;
        for(int i : grid)
        {
            if(i == gameGrid.get("FreeSpin"))
                freeShowCount++;
        }

        resp.put("Award", award);
        resp.put("PayTable", awardPayTable);
        resp.put("Times", times);
        resp.put("Win", win);
        resp.put("FreeShowCount", freeShowCount);
        resp.put("SuperSpinCount", freeShowCount * superToSuperCount);

        return resp;
    }

    private int getSuperTimes()
    {
        int times = 0;

        //權重轉移
        int totalWeight = 0;
        int[] range = new int[superTimesWeight.length];
        for(int i = 0; i < superTimesWeight.length; i++)
        {
            totalWeight += superTimesWeight[i];

            for(int j = 0; j <= i; j++)
                range[i] += superTimesWeight[j];
        }

        //取符合權重 倍率
        int rand = (int) (Math.random() * totalWeight);
        for(int i = 0; i < range.length; i++)
        {
            if(rand > range[i])
                times = superTimesRange[i];
        }

        return times;
    }



    //////////////////////////////////////////////
    //工具
    //////////////////////////////////////////////

    //總押注分數
    public long getTotalBet(long lineBet){return lineBet * betTimes;}

    //遊戲編號
    public int getGameID() {return this.gameID;}

    //幾格
    public int getMaxGrid() {return this.maxGrid;}

    //幾軸
    public int getMaxReel() {return this.maxReel;}

    //線數
    public int getMaxLine() {return this.maxLine;}

    //押注線數
    public int getBetTimes() {return this.betTimes;}

    //免費局數
    public int getFreeCount() {return this.freeCount;}

    //超級局數
    public int getSuperCount() {return this.superCount;}

    //超級中超級局數
    public int getSuperToSuperCount() {return this.superToSuperCount;}

    //免中免局數
    public int getFreeToFreeCount() {return this.freeToFreeCount;}

    //取圖標表
    public HashMap<String, Integer> getGameGrid(){return this.gameGrid;}

    //取賠付表
    public int[] getPayTable() {return this.payTable;}

    //讀取測試表
    private int[][] loadTestProbXls(int gameState)
    {
        int[][] wheel = new int[this.maxReel][];

        try {
            Workbook workbook = Workbook.getWorkbook(new File("D:/test/testProb.xls"));
            Sheet sheet = null;

            if(gameState == 0)
                sheet = workbook.getSheet(0);
            else if(gameState == 1)
                sheet = workbook.getSheet(1);
            else if(gameState == 2)
                sheet = workbook.getSheet(2);

            //轉輪
            for(int i = 0; i < this.maxReel; i++)
            {
                List<Integer> list = new ArrayList<Integer>();

                for(int j = 0; j < sheet.getRows(); j++)
                {
                    if(sheet.getCell(i, j).getContents().length() > 0)
                    {
                        list.add(Integer.parseInt(sheet.getCell(i, j).getContents()));
                    }
                }

                wheel[i] = new int[list.size()];

                for(int j = 0; j < list.size(); j++)
                    wheel[i][j] = list.get(j);
            }

            sheet = workbook.getSheet(0);

            //賠率表
            for(int i = 0; i < payTable.length; i++)
            {
//					if(sheet.getCell(j+7, i+14).getContents().length() > 0)
//						payTable[i] = Integer.parseInt(sheet.getCell(j+7, i+14).getContents());
            }

            workbook.close();
        } catch (BiffException e) {
            System.out.println("load error," + e);
        } catch (IOException e) {
            System.out.println("load error," + e);
        }

        System.out.println(wheel[0][0] + "," + wheel[1][0] + "," + wheel[2][0] + "," + wheel[3][0] + "," + wheel[4][0]);

        return wheel;
    }




}
