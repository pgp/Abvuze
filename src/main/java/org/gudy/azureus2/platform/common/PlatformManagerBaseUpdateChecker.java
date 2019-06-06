package org.gudy.azureus2.platform.common;

/**
 * @author PGP
 * Common code, refactored out of win & macos classes
 */

import java.io.*;
import java.util.*;
import java.net.*;

import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.core3.html.HTMLUtils;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.platform.*;
import org.gudy.azureus2.plugins.utils.resourcedownloader.*;
import org.gudy.azureus2.pluginsimpl.local.utils.resourcedownloader.*;
import org.gudy.azureus2.plugins.*;
import org.gudy.azureus2.plugins.update.*;
import org.gudy.azureus2.pluginsimpl.update.sf.*;

public abstract class PlatformManagerBaseUpdateChecker implements Plugin, UpdatableComponent {
    protected static final LogIDs LOGID = LogIDs.CORE;
    public static final String UPDATE_NAME	= "Platform-specific support";

    public static final int	RD_SIZE_RETRIES	= 3;
    public static final int	RD_SIZE_TIMEOUT	= 10000;

    protected PluginInterface			plugin_interface;

    protected int PLATFORM_TYPE;
    protected String OSTag;

    public void
    initialize(
            PluginInterface	_plugin_interface)
    {
        plugin_interface	= _plugin_interface;

        plugin_interface.getPluginProperties().setProperty( "plugin.name", "Platform-Specific Support" );

        String	version = "1.0";	// default version if plugin not present

        PlatformManager platform	= PlatformManagerFactory.getPlatformManager();

        if ( platform.getPlatformType() == PLATFORM_TYPE ){

            if ( platform.hasCapability( PlatformManagerCapabilities.GetVersion )){

                try{
                    version = platform.getVersion();

                }catch( Throwable e ){

                    Debug.printStackTrace(e);
                }
            }

            plugin_interface.getUpdateManager().registerUpdatableComponent( this, false );

        }else{

            plugin_interface.getPluginProperties().setProperty( "plugin.version.info", "Not required for this platform" );

        }

        plugin_interface.getPluginProperties().setProperty( "plugin.version", version );
    }

    public String
    getName()
    {
        return( UPDATE_NAME );
    }

    public int
    getMaximumCheckTime()
    {
        return(( RD_SIZE_RETRIES * RD_SIZE_TIMEOUT )/1000);
    }

    public void
    checkForUpdate(
            final UpdateChecker	checker )
    {
        try{
            SFPluginDetails	sf_details = SFPluginDetailsLoaderFactory.getSingleton().getPluginDetails( plugin_interface.getPluginID());

            String	current_version = plugin_interface.getPluginVersion();

            if (Logger.isEnabled())
                Logger.log(new LogEvent(LOGID,
                        "PlatformManager:"+OSTag+" update check starts: current = "
                                + current_version));

            boolean current_az_is_cvs	= Constants.isCVSVersion();

            String sf_plugin_version	= sf_details.getVersion();

            String sf_comp_version	 	= sf_plugin_version;

            if ( current_az_is_cvs ){

                String	sf_cvs_version = sf_details.getCVSVersion();

                if ( sf_cvs_version.length() > 0 ){

                    // sf cvs version ALWAYS entry in _CVS

                    sf_plugin_version	= sf_cvs_version;

                    sf_comp_version = sf_plugin_version.substring(0,sf_plugin_version.length()-4);
                }
            }

            String	target_version	= null;

            if (	 sf_comp_version.length() == 0 ||
                    !Character.isDigit(sf_comp_version.charAt(0))){

                if (Logger.isEnabled())
                    Logger.log(new LogEvent(LOGID, LogEvent.LT_WARNING,
                            "PlatformManager:"+OSTag+" no valid version to check against ("
                                    + sf_comp_version + ")"));

            }else if ( Constants.compareVersions( current_version, sf_comp_version ) < 0 ){

                target_version	= sf_comp_version;
            }

            checker.reportProgress( OSTag+": current = " + current_version + ", latest = " + sf_comp_version );

            if (Logger.isEnabled())
                Logger.log(new LogEvent(LOGID,
                        "PlatformManager:"+OSTag+" update required = "
                                + (target_version != null)));

            if ( target_version != null ){

                String target_download		= sf_details.getDownloadURL();

                if ( current_az_is_cvs ){

                    String	sf_cvs_version = sf_details.getCVSVersion();

                    if ( sf_cvs_version.length() > 0 ){

                        target_download	= sf_details.getCVSDownloadURL();
                    }
                }

                ResourceDownloaderFactory rdf = ResourceDownloaderFactoryImpl.getSingleton();

                ResourceDownloader direct_rdl = rdf.create( new URL( target_download ));

                String	torrent_download = Constants.AELITIS_TORRENTS;

                int	slash_pos = target_download.lastIndexOf("/");

                if ( slash_pos == -1 ){

                    torrent_download += target_download;

                }else{

                    torrent_download += target_download.substring( slash_pos + 1 );
                }

                torrent_download	+= ".torrent";

                ResourceDownloader torrent_rdl = rdf.create( new URL( torrent_download ));

                torrent_rdl	= rdf.getSuffixBasedDownloader( torrent_rdl );

                // create an alternate downloader with torrent attempt first

                ResourceDownloader alternate_rdl = rdf.getAlternateDownloader( new ResourceDownloader[]{ torrent_rdl, direct_rdl });

                // get size here so it is cached

                rdf.getTimeoutDownloader(rdf.getRetryDownloader(alternate_rdl,RD_SIZE_RETRIES),RD_SIZE_TIMEOUT).getSize();


                List	desc_lines = HTMLUtils.convertHTMLToText( "", sf_details.getDescription());

                List update_desc = new ArrayList(desc_lines);

                List	comment_lines = HTMLUtils.convertHTMLToText( "    ", sf_details.getComment());

                update_desc.addAll( comment_lines );

                String[]	update_d = new String[update_desc.size()];

                update_desc.toArray( update_d );

                final Update	update =
                        checker.addUpdate(
                                UPDATE_NAME,
                                update_d,
                                current_version,
                                target_version,
                                alternate_rdl,
                                Update.RESTART_REQUIRED_YES );

                update.setDescriptionURL(sf_details.getInfoURL());

                alternate_rdl.addListener(
                        new ResourceDownloaderAdapter()
                        {
                            public boolean
                            completed(
                                    final ResourceDownloader	downloader,
                                    InputStream					data )
                            {
                                installUpdate( checker, update, downloader, data );

                                return( true );
                            }

                            public void
                            failed(
                                    ResourceDownloader			downloader,
                                    ResourceDownloaderException e )
                            {
                                Debug.out( downloader.getName() + " failed", e );

                                update.complete( false );
                            }
                        });
            }
        }catch( Throwable e ){

            Debug.printStackTrace( e );

            checker.reportProgress( "Failed to load plugin details for the platform manager: " + Debug.getNestedExceptionMessage(e));

            checker.failed();

        }finally{

            checker.completed();
        }
    }

    protected abstract void
    installUpdate(UpdateChecker checker, Update update, ResourceDownloader rd, InputStream data);

    protected List
    splitMultiLine(
            String		indent,
            String		text )
    {
        int		pos = 0;

        String	lc_text = text.toLowerCase();

        List	lines = new ArrayList();

        while( true ){

            String	line;

            int	p1 = lc_text.indexOf( "<br>", pos );

            if ( p1 == -1 ){

                line = text.substring(pos);

            }else{

                line = text.substring(pos,p1);

                pos = p1+4;
            }

            lines.add( indent + line );

            if ( p1 == -1 ){

                break;
            }
        }

        return( lines );
    }
}

