package org.gudy.azureus2.platform.common;

import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.FileUtil;
import org.gudy.azureus2.platform.PlatformManager;
import org.gudy.azureus2.platform.PlatformManagerCapabilities;
import org.gudy.azureus2.plugins.platform.PlatformManagerException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PlatformManagerBaseImpl implements PlatformManager {

    public final Set<PlatformManagerCapabilities> capabilitySet = new HashSet<>();

    public Class<?> loadClass(ClassLoader loader, String class_name) throws PlatformManagerException {
        try {
            return loader.loadClass(class_name);
        }
        catch(Throwable e) {
            throw new PlatformManagerException("load of '" + class_name + "' failed", e);
        }
    }

    public boolean hasCapability(PlatformManagerCapabilities capability) {
        return capabilitySet.contains(capability);
    }

    public void checkCapability(PlatformManagerCapabilities capability) throws PlatformManagerException {
        if(!hasCapability(capability)) {
            throw new PlatformManagerException("Capability " + capability + " not supported" );
        }
    }

    protected abstract File[] getJVMOptionFiles();

    protected abstract String getJVMOptionRedirect();

    protected File checkAndGetLocalVMOptionFile() throws PlatformManagerException {
        String vendor = System.getProperty( "java.vendor", "<unknown>" );

        if ( !vendor.toLowerCase().startsWith( "sun " ) && !vendor.toLowerCase().startsWith( "oracle " )){

            throw( new PlatformManagerException(
                    MessageText.getString(
                            "platform.jvmopt.sunonly",
                            new String[]{ vendor })));
        }

        File[] option_files = getJVMOptionFiles();

        if ( option_files.length != 2 ){

            throw( new PlatformManagerException(
                    MessageText.getString( "platform.jvmopt.configerror" )));
        }

        File shared_options = option_files[0];

        if ( shared_options.exists()){

            try{
                String s_options = FileUtil.readFileAsString( shared_options, -1 );

                if ( s_options.contains( getJVMOptionRedirect() )){

                    File local_options = option_files[1];

                    return( local_options );

                }else{

                    throw( new PlatformManagerException( MessageText.getString( "platform.jvmopt.nolink" )));
                }
            }catch( Throwable e ){

                throw( new PlatformManagerException( MessageText.getString( "platform.jvmopt.accesserror", new String[]{ Debug.getNestedExceptionMessage(e) } )));
            }
        }else{

            throw( new PlatformManagerException( MessageText.getString( "platform.jvmopt.nolinkfile" )));
        }
    }

    public File
    getVMOptionFile()

            throws PlatformManagerException
    {
        checkCapability( PlatformManagerCapabilities.AccessExplicitVMOptions );

        File local_options = checkAndGetLocalVMOptionFile();

        if ( !local_options.exists()){

            try{
                local_options.createNewFile();

            }catch( Throwable e ){
            }
        }

        return( local_options );
    }

    public String[]
    getExplicitVMOptions()

            throws PlatformManagerException
    {
        checkCapability( PlatformManagerCapabilities.AccessExplicitVMOptions );


        File local_options = checkAndGetLocalVMOptionFile();

        try{

            List<String> list = new ArrayList<>();

            if ( local_options.exists()){

                try (LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(local_options), StandardCharsets.UTF_8))) {
                    while (true) {

                        String line = lnr.readLine();

                        if (line == null) {

                            break;
                        }

                        line = line.trim();

                        if (line.length() > 0) {

                            list.add(line);
                        }
                    }

                }
            }

            return( list.toArray(new String[0]));

        }catch( Throwable e ){

            throw( new PlatformManagerException( MessageText.getString( "platform.jvmopt.accesserror", new String[]{ Debug.getNestedExceptionMessage(e) } )));
        }
    }

    public void
    setExplicitVMOptions(
            String[]		options )

            throws PlatformManagerException
    {
        checkCapability( PlatformManagerCapabilities.AccessExplicitVMOptions );

        File local_options = checkAndGetLocalVMOptionFile();

        try{
            if ( local_options.exists()){

                File backup = new File( local_options.getParentFile(), local_options.getName() + ".bak" );

                if ( backup.exists()){

                    backup.delete();
                }

                if ( !local_options.renameTo( backup )){

                    throw( new Exception( "Failed to move " + local_options + " to " + backup ));
                }

                boolean	ok = false;

                try{

                    try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(local_options), StandardCharsets.UTF_8))) {
                        for (String option : options) {

                            pw.println(option);
                        }

                        ok = true;

                    }
                }finally{

                    if ( !ok ){

                        local_options.delete();

                        backup.renameTo( local_options );
                    }
                }
            }
        }catch( Throwable e ){

            throw( new PlatformManagerException( MessageText.getString( "platform.jvmopt.accesserror", new String[]{ Debug.getNestedExceptionMessage(e) } )));
        }
    }
}
