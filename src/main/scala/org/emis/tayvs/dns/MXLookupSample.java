package org.emis.tayvs.dns;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class MXLookupSample {
    public static void main(String args[]) {
        if (args.length == 0) {
            System.err.println("Usage: MXLookup host [...]");
            System.exit(99);
        }
        for (String arg : args) {
            try {
                System.out.println(arg + " has " + doLookup(arg) + " mail servers\n");
            } catch (Exception e) {
                System.out.println(arg + " : " + e.getMessage() + "\n");
            }
        }
    }

    static int doLookup(String hostName) throws NamingException {
        Hashtable<String, String> env = new Hashtable();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        DirContext ictx = new InitialDirContext(env);

        String attributeType = "A"; //"MX"

        Attributes attrs = ictx.getAttributes(hostName, new String[]{attributeType});
        Attribute attr = attrs.get(attributeType);
        if (attr == null) return (0);
        for (int i = 0; i < attr.size(); i++) System.out.println(attr.get(i));
        return (attr.size());
    }
}
