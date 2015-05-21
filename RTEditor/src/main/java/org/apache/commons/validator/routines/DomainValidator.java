/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.validator.routines;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.onegravity.rteditor.utils.Helper;

import android.os.AsyncTask;
import android.util.Log;

/**
 * <p><b>Domain name</b> validation routines.</p>
 *
 * <p>
 * This validator provides methods for validating Internet domain names
 * and top-level domains.
 * </p>
 *
 * <p>Domain names are evaluated according
 * to the standards <a href="http://www.ietf.org/rfc/rfc1034.txt">RFC1034</a>,
 * section 3, and <a href="http://www.ietf.org/rfc/rfc1123.txt">RFC1123</a>,
 * section 2.1. No accomodation is provided for the specialized needs of
 * other applications; if the domain name has been URL-encoded, for example,
 * validation will fail even though the equivalent plaintext version of the
 * same name would have passed.
 * </p>
 *
 * <p>
 * Validation is also provided for top-level domains (TLDs) as defined and
 * maintained by the Internet Assigned Numbers Authority (IANA):
 * </p>
 *
 *   <ul>
 *     <li>{@link #isValidInfrastructureTld} - validates infrastructure TLDs
 *         (<code>.arpa</code>, etc.)</li>
 *     <li>{@link #isValidGenericTld} - validates generic TLDs
 *         (<code>.com, .org</code>, etc.)</li>
 *     <li>{@link #isValidCountryCodeTld} - validates country code TLDs
 *         (<code>.us, .uk, .cn</code>, etc.)</li>
 *   </ul>
 *
 * <p>
 * (<b>NOTE</b>: This class does not provide IP address lookup for domain names or
 * methods to ensure that a given domain name matches a specific IP; see
 * {@link java.net.InetAddress} for that functionality.)
 * </p>
 */
public class DomainValidator implements Serializable {

    private static final long serialVersionUID = -4407125112880174009L;

    // Regular expression strings for hostnames (derived from RFC2396 and RFC 1123)
    private static final String DOMAIN_LABEL_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]*\\p{Alnum})*";
    private static final String TOP_LABEL_REGEX = "\\p{Alpha}{2,}";
    private static final String DOMAIN_NAME_REGEX =
            "^(?:" + DOMAIN_LABEL_REGEX + "\\.)+" + "(" + TOP_LABEL_REGEX + ")$";

    private final boolean mAllowLocal;

    private static boolean sTLDLoaded;
    private static boolean sTLDLoading;

    /**
     * Singleton instance of this validator, which
     *  doesn't consider local addresses as valid.
     */
    private static DomainValidator sDOMAIN_VALIDATOR;

    /**
     * Singleton instance of this validator, which does
     *  consider local addresses valid.
     */
    private static DomainValidator sDOMAIN_VALIDATOR_WITH_LOCAL;

    /**
     * RegexValidator for matching domains.
     */
    private final RegexValidator mDomainRegex = new RegexValidator(DOMAIN_NAME_REGEX);
    /**
     * RegexValidator for matching the a local hostname
     */
    private final RegexValidator mHostnameRegex = new RegexValidator(DOMAIN_LABEL_REGEX);

    /**
     * Returns the singleton instance of this validator. It
     *  will not consider local addresses as valid.
     * @return the singleton instance of this validator
     */
    public static DomainValidator getInstance() {
    	if (sDOMAIN_VALIDATOR == null) {
    		sDOMAIN_VALIDATOR = new DomainValidator(false);
    	}
        return sDOMAIN_VALIDATOR;
    }

    /**
     * Returns the singleton instance of this validator,
     *  with local validation as required.
     * @param allowLocal Should local addresses be considered valid?
     * @return the singleton instance of this validator
     */
    public static DomainValidator getInstance(boolean allowLocal) {
    	if(allowLocal) {
    		if (sDOMAIN_VALIDATOR_WITH_LOCAL == null) {
           		sDOMAIN_VALIDATOR_WITH_LOCAL = new DomainValidator(true);
        	}
            return sDOMAIN_VALIDATOR_WITH_LOCAL;
        }
        return getInstance();
    }

    /** Private constructor. */
    private DomainValidator(boolean allowLocal) {
    	loadTLD();
        mAllowLocal = allowLocal;
    }

    private void loadTLD() {
    	if (! sTLDLoaded && ! sTLDLoading) {
			try {
				sTLDLoading = true;
				URL url = new URL(TLDS_BY_IANA);
	    		new DownloadFilesTask().execute(url);
			} catch (MalformedURLException ignore) {
				sTLDLoading = false;
			} 
    	}
    }

    private class DownloadFilesTask extends AsyncTask<URL, Void, Void> {
    	@Override
        protected Void doInBackground(URL... urls) {
    		URL url = urls[0];

    		List<String> newTLDs = new ArrayList<String>();
    		
            BufferedReader reader = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                reader = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
                for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                	if (! s.startsWith("#")) {
                		newTLDs.add(s.trim());
                	}
                }
                /*
                 * If the device is connected but can't connect to the site we ignore the result.
                 * It will very likely be empty but be ignore every domain list shorter than the one we already got. 
                 */
                if (newTLDs.size() > TLDS.length) {
                	synchronized(sTLD_LIST) {
                		sTLD_LIST = newTLDs;
                	}
                	sTLDLoaded = true;
                }
            }
            catch (Exception ignore) {
            	Log.w(getClass().getSimpleName(), ignore.getMessage());
            }
            finally {
            	Helper.closeQuietly(reader);
            	connection.disconnect();
            }
			sTLDLoading = false;
            return null;
        }
    }
    
    /**
     * Returns true if the specified <code>String</code> parses
     * as a valid domain name with a recognized top-level domain.
     * The parsing is case-sensitive.
     * @param domain the parameter to check for domain name syntax
     * @return true if the parameter is a valid domain name
     */
    public boolean isValid(String domain) {
        loadTLD();
        String[] groups = mDomainRegex.match(domain);
        if (groups != null && groups.length > 0) {
            return isValidTld(groups[0]);
        } else if(mAllowLocal) {
            if (mHostnameRegex.isValid(domain)) {
               return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the specified <code>String</code> matches any
     * IANA-defined top-level domain. Leading dots are ignored if present.
     * The search is case-sensitive.
     * @param tld the parameter to check for TLD status
     * @return true if the parameter is a TLD
     */
    private boolean isValidTld(String tld) {
        if(mAllowLocal && isValidLocalTld(tld)) {
           return true;
        }
        synchronized(sTLD_LIST) {
        	return sTLD_LIST.contains(chompLeadingDot(tld.toUpperCase(Locale.getDefault())));
        }
    }

    /**
     * Returns true if the specified <code>String</code> matches any
     * widely used "local" domains (localhost or localdomain). Leading dots are
     *  ignored if present. The search is case-sensitive.
     * @param iTld the parameter to check for local TLD status
     * @return true if the parameter is an local TLD
     */
    public boolean isValidLocalTld(String iTld) {
        return LOCAL_TLD_LIST.contains(chompLeadingDot(iTld.toLowerCase(Locale.getDefault())));
    }

    private String chompLeadingDot(String str) {
        if (str.startsWith(".")) {
            return str.substring(1);
        } else {
            return str;
        }
    }

    /* 
     * TLDs defined by IANA
     * Authoritative and comprehensive list at:
     * 
     * http://data.iana.org/TLD/tlds-alpha-by-domain.txt
     * 
     * # Version 2014091501, Last Updated Tue Sep 16 07:07:01 2014 UTC
     */
    private static final String TLDS_BY_IANA = "http://data.iana.org/TLD/tlds-alpha-by-domain.txt";
    
    private static final String[] TLDS = new String[] {
    	"AC",
    	"ACADEMY",
    	"ACCOUNTANTS",
    	"ACTIVE",
    	"ACTOR",
    	"AD",
    	"AE",
    	"AERO",
    	"AF",
    	"AG",
    	"AGENCY",
    	"AI",
    	"AIRFORCE",
    	"AL",
    	"AM",
    	"AN",
    	"AO",
    	"AQ",
    	"AR",
    	"ARCHI",
    	"ARMY",
    	"ARPA",
    	"AS",
    	"ASIA",
    	"ASSOCIATES",
    	"AT",
    	"ATTORNEY",
    	"AU",
    	"AUCTION",
    	"AUDIO",
    	"AUTOS",
    	"AW",
    	"AX",
    	"AXA",
    	"AZ",
    	"BA",
    	"BAR",
    	"BARGAINS",
    	"BAYERN",
    	"BB",
    	"BD",
    	"BE",
    	"BEER",
    	"BERLIN",
    	"BEST",
    	"BF",
    	"BG",
    	"BH",
    	"BI",
    	"BID",
    	"BIKE",
    	"BIO",
    	"BIZ",
    	"BJ",
    	"BLACK",
    	"BLACKFRIDAY",
    	"BLUE",
    	"BM",
    	"BMW",
    	"BN",
    	"BNPPARIBAS",
    	"BO",
    	"BOO",
    	"BOUTIQUE",
    	"BR",
    	"BRUSSELS",
    	"BS",
    	"BT",
    	"BUILD",
    	"BUILDERS",
    	"BUSINESS",
    	"BUZZ",
    	"BV",
    	"BW",
    	"BY",
    	"BZ",
    	"BZH",
    	"CA",
    	"CAB",
    	"CAL",
    	"CAMERA",
    	"CAMP",
    	"CANCERRESEARCH",
    	"CAPETOWN",
    	"CAPITAL",
    	"CARAVAN",
    	"CARDS",
    	"CARE",
    	"CAREER",
    	"CAREERS",
    	"CASH",
    	"CAT",
    	"CATERING",
    	"CC",
    	"CD",
    	"CENTER",
    	"CEO",
    	"CERN",
    	"CF",
    	"CG",
    	"CH",
    	"CHANNEL",
    	"CHEAP",
    	"CHRISTMAS",
    	"CHROME",
    	"CHURCH",
    	"CI",
    	"CITIC",
    	"CITY",
    	"CK",
    	"CL",
    	"CLAIMS",
    	"CLEANING",
    	"CLICK",
    	"CLINIC",
    	"CLOTHING",
    	"CLUB",
    	"CM",
    	"CN",
    	"CO",
    	"CODES",
    	"COFFEE",
    	"COLLEGE",
    	"COLOGNE",
    	"COM",
    	"COMMUNITY",
    	"COMPANY",
    	"COMPUTER",
    	"CONDOS",
    	"CONSTRUCTION",
    	"CONSULTING",
    	"CONTRACTORS",
    	"COOKING",
    	"COOL",
    	"COOP",
    	"COUNTRY",
    	"CR",
    	"CREDIT",
    	"CREDITCARD",
    	"CRUISES",
    	"CU",
    	"CUISINELLA",
    	"CV",
    	"CW",
    	"CX",
    	"CY",
    	"CYMRU",
    	"CZ",
    	"DAD",
    	"DANCE",
    	"DATING",
    	"DAY",
    	"DE",
    	"DEALS",
    	"DEGREE",
    	"DEMOCRAT",
    	"DENTAL",
    	"DENTIST",
    	"DESI",
    	"DIAMONDS",
    	"DIET",
    	"DIGITAL",
    	"DIRECT",
    	"DIRECTORY",
    	"DISCOUNT",
    	"DJ",
    	"DK",
    	"DM",
    	"DNP",
    	"DO",
    	"DOMAINS",
    	"DURBAN",
    	"DZ",
    	"EAT",
    	"EC",
    	"EDU",
    	"EDUCATION",
    	"EE",
    	"EG",
    	"EMAIL",
    	"ENGINEER",
    	"ENGINEERING",
    	"ENTERPRISES",
    	"EQUIPMENT",
    	"ER",
    	"ES",
    	"ESQ",
    	"ESTATE",
    	"ET",
    	"EU",
    	"EUS",
    	"EVENTS",
    	"EXCHANGE",
    	"EXPERT",
    	"EXPOSED",
    	"FAIL",
    	"FARM",
    	"FEEDBACK",
    	"FI",
    	"FINANCE",
    	"FINANCIAL",
    	"FISH",
    	"FISHING",
    	"FITNESS",
    	"FJ",
    	"FK",
    	"FLIGHTS",
    	"FLORIST",
    	"FLY",
    	"FM",
    	"FO",
    	"FOO",
    	"FOUNDATION",
    	"FR",
    	"FRL",
    	"FROGANS",
    	"FUND",
    	"FURNITURE",
    	"FUTBOL",
    	"GA",
    	"GAL",
    	"GALLERY",
    	"GB",
    	"GBIZ",
    	"GD",
    	"GE",
    	"GENT",
    	"GF",
    	"GG",
    	"GH",
    	"GI",
    	"GIFT",
    	"GIFTS",
    	"GIVES",
    	"GL",
    	"GLASS",
    	"GLE",
    	"GLOBAL",
    	"GLOBO",
    	"GM",
    	"GMAIL",
    	"GMO",
    	"GMX",
    	"GN",
    	"GOOGLE",
    	"GOP",
    	"GOV",
    	"GP",
    	"GQ",
    	"GR",
    	"GRAPHICS",
    	"GRATIS",
    	"GREEN",
    	"GRIPE",
    	"GS",
    	"GT",
    	"GU",
    	"GUIDE",
    	"GUITARS",
    	"GURU",
    	"GW",
    	"GY",
    	"HAMBURG",
    	"HAUS",
    	"HEALTHCARE",
    	"HELP",
    	"HERE",
    	"HIPHOP",
    	"HIV",
    	"HK",
    	"HM",
    	"HN",
    	"HOLDINGS",
    	"HOLIDAY",
    	"HOMES",
    	"HORSE",
    	"HOST",
    	"HOSTING",
    	"HOUSE",
    	"HOW",
    	"HR",
    	"HT",
    	"HU",
    	"ID",
    	"IE",
    	"IL",
    	"IM",
    	"IMMO",
    	"IMMOBILIEN",
    	"IN",
    	"INDUSTRIES",
    	"INFO",
    	"ING",
    	"INK",
    	"INSTITUTE",
    	"INSURE",
    	"INT",
    	"INTERNATIONAL",
    	"INVESTMENTS",
    	"IO",
    	"IQ",
    	"IR",
    	"IS",
    	"IT",
    	"JE",
    	"JETZT",
    	"JM",
    	"JO",
    	"JOBS",
    	"JOBURG",
    	"JP",
    	"JUEGOS",
    	"KAUFEN",
    	"KE",
    	"KG",
    	"KH",
    	"KI",
    	"KIM",
    	"KITCHEN",
    	"KIWI",
    	"KM",
    	"KN",
    	"KOELN",
    	"KP",
    	"KR",
    	"KRD",
    	"KRED",
    	"KW",
    	"KY",
    	"KZ",
    	"LA",
    	"LACAIXA",
    	"LAND",
    	"LAWYER",
    	"LB",
    	"LC",
    	"LEASE",
    	"LGBT",
    	"LI",
    	"LIFE",
    	"LIGHTING",
    	"LIMITED",
    	"LIMO",
    	"LINK",
    	"LK",
    	"LOANS",
    	"LONDON",
    	"LOTTO",
    	"LR",
    	"LS",
    	"LT",
    	"LTDA",
    	"LU",
    	"LUXE",
    	"LUXURY",
    	"LV",
    	"LY",
    	"MA",
    	"MAISON",
    	"MANAGEMENT",
    	"MANGO",
    	"MARKET",
    	"MARKETING",
    	"MC",
    	"MD",
    	"ME",
    	"MEDIA",
    	"MEET",
    	"MELBOURNE",
    	"MEME",
    	"MENU",
    	"MG",
    	"MH",
    	"MIAMI",
    	"MIL",
    	"MINI",
    	"MK",
    	"ML",
    	"MM",
    	"MN",
    	"MO",
    	"MOBI",
    	"MODA",
    	"MOE",
    	"MONASH",
    	"MORTGAGE",
    	"MOSCOW",
    	"MOTORCYCLES",
    	"MOV",
    	"MP",
    	"MQ",
    	"MR",
    	"MS",
    	"MT",
    	"MU",
    	"MUSEUM",
    	"MV",
    	"MW",
    	"MX",
    	"MY",
    	"MZ",
    	"NA",
    	"NAGOYA",
    	"NAME",
    	"NAVY",
    	"NC",
    	"NE",
    	"NET",
    	"NETWORK",
    	"NEUSTAR",
    	"NEW",
    	"NEXUS",
    	"NF",
    	"NG",
    	"NGO",
    	"NHK",
    	"NI",
    	"NINJA",
    	"NL",
    	"NO",
    	"NP",
    	"NR",
    	"NRA",
    	"NRW",
    	"NU",
    	"NYC",
    	"NZ",
    	"OKINAWA",
    	"OM",
    	"ONG",
    	"ONL",
    	"OOO",
    	"ORG",
    	"ORGANIC",
    	"OTSUKA",
    	"OVH",
    	"PA",
    	"PARIS",
    	"PARTNERS",
    	"PARTS",
    	"PE",
    	"PF",
    	"PG",
    	"PH",
    	"PHARMACY",
    	"PHOTO",
    	"PHOTOGRAPHY",
    	"PHOTOS",
    	"PHYSIO",
    	"PICS",
    	"PICTURES",
    	"PINK",
    	"PIZZA",
    	"PK",
    	"PL",
    	"PLACE",
    	"PLUMBING",
    	"PM",
    	"PN",
    	"POST",
    	"PR",
    	"PRAXI",
    	"PRESS",
    	"PRO",
    	"PROD",
    	"PRODUCTIONS",
    	"PROF",
    	"PROPERTIES",
    	"PROPERTY",
    	"PS",
    	"PT",
    	"PUB",
    	"PW",
    	"PY",
    	"QA",
    	"QPON",
    	"QUEBEC",
    	"RE",
    	"REALTOR",
    	"RECIPES",
    	"RED",
    	"REHAB",
    	"REISE",
    	"REISEN",
    	"REN",
    	"RENTALS",
    	"REPAIR",
    	"REPORT",
    	"REPUBLICAN",
    	"REST",
    	"RESTAURANT",
    	"REVIEWS",
    	"RICH",
    	"RIO",
    	"RO",
    	"ROCKS",
    	"RODEO",
    	"RS",
    	"RSVP",
    	"RU",
    	"RUHR",
    	"RW",
    	"RYUKYU",
    	"SA",
    	"SAARLAND",
    	"SARL",
    	"SB",
    	"SC",
    	"SCA",
    	"SCB",
    	"SCHMIDT",
    	"SCHULE",
    	"SCOT",
    	"SD",
    	"SE",
    	"SERVICES",
    	"SEXY",
    	"SG",
    	"SH",
    	"SHIKSHA",
    	"SHOES",
    	"SI",
    	"SINGLES",
    	"SJ",
    	"SK",
    	"SL",
    	"SM",
    	"SN",
    	"SO",
    	"SOCIAL",
    	"SOFTWARE",
    	"SOHU",
    	"SOLAR",
    	"SOLUTIONS",
    	"SOY",
    	"SPACE",
    	"SPIEGEL",
    	"SR",
    	"ST",
    	"SU",
    	"SUPPLIES",
    	"SUPPLY",
    	"SUPPORT",
    	"SURF",
    	"SURGERY",
    	"SUZUKI",
    	"SV",
    	"SX",
    	"SY",
    	"SYSTEMS",
    	"SZ",
    	"TATAR",
    	"TATTOO",
    	"TAX",
    	"TC",
    	"TD",
    	"TECHNOLOGY",
    	"TEL",
    	"TF",
    	"TG",
    	"TH",
    	"TIENDA",
    	"TIPS",
    	"TIROL",
    	"TJ",
    	"TK",
    	"TL",
    	"TM",
    	"TN",
    	"TO",
    	"TODAY",
    	"TOKYO",
    	"TOOLS",
    	"TOP",
    	"TOWN",
    	"TOYS",
    	"TP",
    	"TR",
    	"TRADE",
    	"TRAINING",
    	"TRAVEL",
    	"TT",
    	"TV",
    	"TW",
    	"TZ",
    	"UA",
    	"UG",
    	"UK",
    	"UNIVERSITY",
    	"UNO",
    	"UOL",
    	"US",
    	"UY",
    	"UZ",
    	"VA",
    	"VACATIONS",
    	"VC",
    	"VE",
    	"VEGAS",
    	"VENTURES",
    	"VERSICHERUNG",
    	"VET",
    	"VG",
    	"VI",
    	"VIAJES",
    	"VILLAS",
    	"VISION",
    	"VLAANDEREN",
    	"VN",
    	"VODKA",
    	"VOTE",
    	"VOTING",
    	"VOTO",
    	"VOYAGE",
    	"VU",
    	"WALES",
    	"WANG",
    	"WATCH",
    	"WEBCAM",
    	"WEBSITE",
    	"WED",
    	"WF",
    	"WHOSWHO",
    	"WIEN",
    	"WIKI",
    	"WILLIAMHILL",
    	"WME",
    	"WORKS",
    	"WS",
    	"WTC",
    	"WTF",
    	"XXX",
    	"XYZ",
    	"YACHTS",
    	"YANDEX",
    	"YE",
    	"YOKOHAMA",
    	"YOUTUBE",
    	"YT",
    	"ZA",
    	"ZIP",
    	"ZM",
    	"ZONE",
    	"ZW"
    };

    private static final String[] LOCAL_TLDS = new String[] {
       "localhost",           // RFC2606 defined
       "localdomain"          // Also widely used as localhost.localdomain
	};

    private static List<String> sTLD_LIST = Arrays.asList(TLDS);
    private static final List<String> LOCAL_TLD_LIST = Arrays.asList(LOCAL_TLDS);
}