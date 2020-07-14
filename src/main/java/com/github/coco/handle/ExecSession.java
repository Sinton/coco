package com.github.coco.handle;

import com.github.coco.thread.OutPutThread;

import java.net.Socket;

/**
 * @author Yan
 */
public class ExecSession {
    private Socket socket;
    private OutPutThread outPutThread;

    public ExecSession(Socket socket, OutPutThread outPutThread) {
        this.socket = socket;
        this.outPutThread = outPutThread;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public OutPutThread getOutPutThread() {
        return outPutThread;
    }

    public void setOutPutThread(OutPutThread outPutThread) {
        this.outPutThread = outPutThread;
    }
}
