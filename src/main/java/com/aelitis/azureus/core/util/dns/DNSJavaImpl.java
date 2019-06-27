package com.aelitis.azureus.core.util.dns;

import com.aelitis.azureus.core.util.DNSUtils;
import it.pgp.misc.Utils;
import org.gudy.azureus2.core3.util.Constants;
import org.xbill.DNS.*;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

// TODO check special characters against DNSUtilsImpl, find domain with suitable TXT records
public class DNSJavaImpl implements DNSUtils.DNSUtilsIntf {

    public static final String dnsServer = Constants.isAndroid?"8.8.8.8":null;

    public static Lookup getLookup(String name, int type) throws UnknownHostException, TextParseException {
        Lookup l = new Lookup(name,type);
        l.setResolver(new SimpleResolver(dnsServer));
        return l;
    }

    private static final Map<String, String> test_records = Utils.typedMapOf(
            "test1.test.null", "BITTORRENT DENY ALL",
            "test2.test.null", "BITTORRENT",
            "test3.test.null", "BITTORRENT TCP:1 TCP:2 UDP:1 UDP:2",
            "test4.test.null", "BITTORRENT TCP:3",
            "test5.test.null", "BITTORRENT UDP:4");

    @Override
    public DNSUtils.DNSDirContext getInitialDirContext() throws Exception {
        return null;
    }

    @Override
    public DNSUtils.DNSDirContext getDirContextForServer(String dns_server_ip) throws Exception {
        return null;
    }

    public List<Inet6Address> getAllIPV6ByName(String host) throws UnknownHostException{
        List<Inet6Address> ret = new ArrayList<>();
        try {
            Record[] recordsV6 = getLookup(host, Type.AAAA).run();
            if(recordsV6!=null)
                for (Record record : recordsV6) {
                    AAAARecord a = (AAAARecord) record;
                    ret.add((Inet6Address) a.getAddress());
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(ret.size()==0) throw new UnknownHostException(host);
        return ret;
    }

    @Override
    public Inet6Address getIPV6ByName(String host) throws UnknownHostException {
        List<Inet6Address> all = getAllIPV6ByName(host);
        return all.get(0);
    }

    @Override
    public List<InetAddress> getAllByName(String host) throws UnknownHostException {
        List<InetAddress> ret = new ArrayList<>();
        try {
            Record[] records = getLookup(host, Type.A).run();
            Record[] recordsV6 = getLookup(host, Type.AAAA).run();
            if(records!=null)
                for (Record record : records) {
                    ARecord a = (ARecord) record;
                    ret.add(a.getAddress());
                }
            if(recordsV6!=null)
                for (Record record : recordsV6) {
                    AAAARecord a = (AAAARecord) record;
                    ret.add(a.getAddress());
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(ret.size()==0) throw new UnknownHostException(host);
        return ret;
    }

    @Override
    public List<InetAddress> getAllByName(DNSUtils.DNSDirContext unused, String host) throws UnknownHostException {
        return getAllByName(host);
    }

    @Override
    public List<String> getTXTRecords(String query) {

        String test_reply = test_records.get(query);
        if (test_reply != null)
            return Arrays.asList(test_reply);

        List<String> ret = new ArrayList<>();
        try {
            Record[] records = getLookup(query, Type.TXT).run();
            if(records!=null)
                for (Record record : records) {
                    TXTRecord txt = (TXTRecord) record;
                    ret.addAll(txt.getStrings());
                }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public String getTXTRecord(String query) throws UnknownHostException {
        List<String> txtrecords = getTXTRecords(query);
        if (txtrecords.size()==0)
            throw (new UnknownHostException("DNS query returned no results"));
        return txtrecords.get(0);
    }
}
