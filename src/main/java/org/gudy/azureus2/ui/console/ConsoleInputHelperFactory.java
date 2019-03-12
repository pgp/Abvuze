package org.gudy.azureus2.ui.console;

import org.gudy.azureus2.ui.console.commands.IConsoleCommand;

import java.io.Reader;

public abstract class ConsoleInputHelperFactory {

    public static ConsoleInputHelperFactory instance; // initialized by classloader, dynamically on Android

    public static CommandReader currentCommandReader;

    public abstract CommandReader getCommandReader(Reader _in);

    public abstract CommandReader getEmptyCommandReader();

    public abstract IConsoleCommand getShowCommand();
}
