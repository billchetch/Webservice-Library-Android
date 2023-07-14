package net.chetch.webservices;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import net.chetch.utilities.SLog;
import net.chetch.webservices.exceptions.WebserviceException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import androidx.lifecycle.Observer;

public class ConnectManager extends Observable {
    public enum ConnectState{
        NOT_SET,
        CONNECT_REQUEST,
        CONNECTING,
        CONNECTED,
        RECONNECT_REQUEST,
        RECONNECTNG,
        DISCONNECT_REQUEST,
        ERROR
    }

    ConnectState previousState = ConnectState.NOT_SET;
    ConnectState currentState = ConnectState.NOT_SET;
    boolean fromError = false;

    List<WebserviceViewModel> models = new ArrayList<>();

    Observer loadObserver;
    Throwable lastError;

    int timerDelay = 5; //in seconds
    boolean timerStarted = false;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            int nextTimer = onTimer();
            if(nextTimer > 0) {
                timerHandler.postDelayed(this, timerDelay * 1000);
            }
        }
    };

    protected void startTimer(int timerDelay, int postDelay){
        if(timerStarted)return;
        this.timerDelay = timerDelay;

        timerHandler.postDelayed(timerRunnable, postDelay*1000);
        timerStarted = true;
    }

    protected void startTimer(int timerDelay){
        startTimer(timerDelay, timerDelay);
    }

    protected void startTimer(){
        startTimer(timerDelay, timerDelay);
    }

    protected void stopTimer(){
        timerHandler.removeCallbacks(timerRunnable);
        timerStarted = false;
    }

    private boolean setConnectState(ConnectState newState){
        boolean isError = newState == ConnectState.ERROR;
        if(isError) {
            newState = (currentState == ConnectState.CONNECTED || currentState == ConnectState.RECONNECTNG || currentState == ConnectState.RECONNECT_REQUEST) ? ConnectState.RECONNECT_REQUEST : ConnectState.CONNECT_REQUEST;
        }

        boolean changed = newState != currentState;
        if(!changed)return false;

        fromError = isError;
        if(SLog.LOG) SLog.i("MAIN", "Current state is " + currentState + " new state is " + newState + " fromError is " + fromError);
        previousState = currentState;
        currentState = newState;

        loadObserver.onChanged(this);

        //observerable stuff
        setChanged();
        notifyObservers(this);

        return changed;
    }

    public ConnectState getState(){
        return currentState;
    }

    public boolean fromError(){
        return fromError;
    }
    public Throwable getLastError(){ return lastError; }

    public boolean modelsReady(){
        boolean ready = true;
        for(WebserviceViewModel m : models){
            if(!m.isReady()){
                ready = false;
                break;
            }
        }
        return ready;
    }

    public static boolean isConnectionError(Throwable throwable) {
        if (
                throwable instanceof WebserviceException ||
                throwable instanceof SocketTimeoutException ||
                throwable instanceof ConnectException ||
                throwable instanceof UnknownHostException ||
                throwable instanceof SocketException
        ){
            return true;
        } else {
            return false;
        }
    }

    protected void handleModelError(WebserviceViewModel model, Throwable throwable){
        if (!model.isReady() || isConnectionError(throwable)) {
            lastError = throwable;
            setConnectState(ConnectState.ERROR);
        }
    }

    public void addModel(WebserviceViewModel model){
        if(!models.contains(model)){
            models.add(model);
            model.getError().observeForever(throwable -> {
                handleModelError(model, throwable);
            });
        }
    }

    public void setPermissableServerTimeDifference(int permissableServerTimeDifference){
        for(WebserviceViewModel m : models){
            m.setPermissableServerTimeDifference(permissableServerTimeDifference);
        }
    }

    public void requestConnect(Observer observer, int timerDelay, int postDelay) throws Exception{
        if(models.size() == 0)throw new Exception("No models added!  Connect request doesn't make sense!");
        loadObserver = observer;

        setConnectState(currentState == ConnectState.CONNECTED ? ConnectState.RECONNECT_REQUEST : ConnectState.CONNECT_REQUEST);
        startTimer(timerDelay, postDelay);
    }

    public void requestConnect(Observer observer, int timerDelay) throws Exception{
        requestConnect(observer, timerDelay, timerDelay);
    }
    public void requestConnect(Observer observer) throws Exception{
        requestConnect(observer, timerDelay);
    }

    public int onTimer(){
        switch(currentState){
            case CONNECT_REQUEST:
            case RECONNECT_REQUEST:
                stopTimer();
                try {
                    setConnectState(currentState == ConnectState.CONNECT_REQUEST ? ConnectState.CONNECTING : ConnectState.RECONNECTNG);
                    for(WebserviceViewModel m : models) {
                        m.loadData(loadObserver);
                    }
                } catch (Exception e){
                    lastError = e;
                    setConnectState(ConnectState.ERROR);
                }
                startTimer(timerDelay);
                break;

            case CONNECTING:
            case RECONNECTNG:
                if(modelsReady()){
                    setConnectState(ConnectState.CONNECTED);
                }
                break;
        }

        return timerDelay;
    }

    public void pause(){
        stopTimer();
        for(WebserviceViewModel model : models){
            model.pause();
        }
    }

    public void resume(){
        startTimer();
        for(WebserviceViewModel model : models){
            model.resume();
        }
    }

    public boolean isConnected(){
        return getState() == ConnectState.CONNECTED;
    }
}
