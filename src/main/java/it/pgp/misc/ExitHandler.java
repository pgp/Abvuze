package it.pgp.misc;

import org.gudy.azureus2.core3.security.SESecurityManager;

public abstract class ExitHandler {
    public static final ExitHandler NoOpHandler = new ExitHandler() {
        @Override
        public void exit(int ret) {}
    };

    public static final ExitHandler stdHandler = new ExitHandler() {
        @Override
        public void exit(int ret) {
            SESecurityManager.exitVM(ret);
        }
    };

    public static ExitHandler defaultHandler = NoOpHandler; // replaced with stdHandler on Android

    public abstract void exit(int ret);
}
