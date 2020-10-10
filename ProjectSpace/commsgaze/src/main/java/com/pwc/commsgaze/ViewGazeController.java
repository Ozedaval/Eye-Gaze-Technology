package com.pwc.commsgaze;


public class ViewGazeController  {

    private static final String TAG = "ViewGazeController";
    private int selectedDataIndex = 0;
    private int fixedDimension;
    private int lastElementIndex;
    private int lastSeqFirstElementIndex;
    private int prevSelectedDataIndex;
    private final int firstElementIndex = 0;
    private int firstSeqLastElementIndex;


    int getSelectedDataIndex(){
        return selectedDataIndex;
    }
    int getPrevSelectedDataIndex(){return prevSelectedDataIndex;}

    void updateSelectedDataIndex(Direction direction){
      /*  Log.d(TAG,"Gaze Direction estimated "+ direction.toString());
        Log.d(TAG,"Current select ViewHolder Index "+ selectedViewHolderIndex);*/
        prevSelectedDataIndex = selectedDataIndex;
        if(hasNeighbourIn(direction, selectedDataIndex)) {

            switch (direction) {
                case LEFT:
                    selectedDataIndex--;
                    break;
                case RIGHT:
                    selectedDataIndex++;
                    break;
                case TOP:
                    selectedDataIndex -=fixedDimension;
                    break;
                case BOTTOM:
                    selectedDataIndex +=fixedDimension;
                    break;
            }
        }
        else{
            if(direction.equals(Direction.TOP)){
                selectedDataIndex = (selectedDataIndex % fixedDimension) + lastSeqFirstElementIndex;
            }
            else if(direction.equals(Direction.BOTTOM)) {
                    selectedDataIndex = (selectedDataIndex % fixedDimension) +firstElementIndex;
            }
        }

 /*       Log.d(TAG,"Updated ViewHolder Index "+ selectedViewHolderIndex);*/
    }



    boolean isOnFirstSeq(int selectedDataIndex){
        return (selectedDataIndex>= firstElementIndex && selectedDataIndex <= firstSeqLastElementIndex);

    }
    boolean isOnLastSeq(int selectedDataIndex){
        return (selectedDataIndex>=lastSeqFirstElementIndex && selectedDataIndex <= lastElementIndex);

    }

    boolean hasNeighbourIn(Direction direction,int selectedDataIndex){
        int remainder = selectedDataIndex % fixedDimension;
        if(direction.equals(Direction.RIGHT)) {
            return remainder + 1 <= fixedDimension - 1;
        }
        else if(direction.equals(Direction.LEFT)){
            return remainder - 1 >= 0;
        }
        else if (direction.equals(Direction.TOP)){
            return !isOnFirstSeq(selectedDataIndex);
        }
        else  if(direction.equals(Direction.BOTTOM)){
            return  !isOnLastSeq(selectedDataIndex);
        }
        return true;
    }



    ViewGazeController(int fixedDimension,int numOfPositions){
        this.fixedDimension = fixedDimension;
        firstSeqLastElementIndex = firstElementIndex + fixedDimension-1;
        lastSeqFirstElementIndex = numOfPositions - fixedDimension;
        lastElementIndex = numOfPositions - 1;
    }

}
