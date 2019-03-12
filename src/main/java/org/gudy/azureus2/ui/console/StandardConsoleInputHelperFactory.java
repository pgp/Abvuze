package org.gudy.azureus2.ui.console;

import org.gudy.azureus2.ui.console.commands.IConsoleCommand;
import org.gudy.azureus2.ui.console.commands.Show;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StandardConsoleInputHelperFactory extends ConsoleInputHelperFactory {

    @Override
    public CommandReader getCommandReader(Reader _in) {
        currentCommandReader = new CommandReader(_in);
        return currentCommandReader;
    }

    @Override
    public CommandReader getEmptyCommandReader() {
        currentCommandReader = new CommandReader( new InputStreamReader( new ByteArrayInputStream(new byte[0])));
        return currentCommandReader;
    }

    @Override
    public IConsoleCommand getShowCommand() {
        return new Show();
    }
}
