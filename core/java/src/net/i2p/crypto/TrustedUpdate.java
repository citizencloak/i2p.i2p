package net.i2p.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.i2p.CoreVersion;
import net.i2p.I2PAppContext;
import net.i2p.data.DataFormatException;
import net.i2p.data.DataHelper;
import net.i2p.data.Signature;
import net.i2p.data.SigningPrivateKey;
import net.i2p.data.SigningPublicKey;
import net.i2p.util.Log;
import net.i2p.util.VersionComparator;
import net.i2p.util.ZipFileComment;

/**
 * <p>Handles DSA signing and verification of update files.
 * </p>
 * <p>For convenience this class also makes certain operations available via the
 * command line. These can be invoked as follows:
 * </p>
 * <pre>
 * java net.i2p.crypto.TrustedUpdate keygen       <i>publicKeyFile privateKeyFile</i>
 * java net.i2p.crypto.TrustedUpdate showversion  <i>signedFile</i>
 * java net.i2p.crypto.TrustedUpdate sign         <i>inputFile signedFile privateKeyFile version</i>
 * java net.i2p.crypto.TrustedUpdate verifysig    <i>signedFile</i>
 * java net.i2p.crypto.TrustedUpdate verifyupdate <i>signedFile</i>
 * java net.i2p.crypto.TrustedUpdate verifyversion <i>signedFile</i>
 * </pre>
 * 
 * @author jrandom and smeghead
 */
public class TrustedUpdate {

    /**
     * <p>Default trusted key generated by jrandom@i2p.net. This can be
     * authenticated via <code>gpg</code> without modification:</p>
     * <p>
     * <code>gpg --verify TrustedUpdate.java</code></p>
     */
/*
-----BEGIN PGP SIGNED MESSAGE-----
Hash: SHA1

*/
    private static final String DEFAULT_TRUSTED_KEY =
        "W4kJbnv9KSVwbnapV7SaNW2kMIZKs~hwL0ro9pZXFo1xTwqz45nykCp1H" +
        "M7sAKYDZay5z1HvYYOl9CNVz00xF03KPU9RUCVxhDZ1YXhZIskPKjUPUs" +
        "CIpE~Z1C~N9KSEV6~2stDlBNH10VZ4T0X1TrcXwb3IBXliWo2y2GAx~Ow=";
/*
-----BEGIN PGP SIGNATURE-----
Version: GnuPG v1.2.4 (GNU/Linux)

iD8DBQFCZ38IWYfZ3rPnHH0RAgOHAJ4wNgmfO2AkL8IXiGnPtWrTlXcVogCfQ79z
jP69nPbh4KLGhF+SD0+0bW4=
=npPe
-----END PGP SIGNATURE-----
*/
/*
-----BEGIN PGP SIGNED MESSAGE-----
Hash: SHA1

*/
    /* zzz's key */
    private static final String DEFAULT_TRUSTED_KEY2 =
        "lT54eq3SH0TWWwQ1wgH6XPelIno7wH7UfiZOpQg-ZuxdNhc4UjjrohKdK" +
        "Zqfswt1ANPnmOlMewLGBESl7kJB9c5sByz~IOlNyz5BMLRC~R~ZC9QI4W" +
        "XwUBYW8BhYO2mkvtdOrcy690lDkwzdf5xLxlCBpQlTaLYzQVjVWBcvbCA=";
/*
-----BEGIN PGP SIGNATURE-----
Version: GnuPG v1.4.6 (GNU/Linux)

iD8DBQFHdupcQVV2uqduC+0RAocuAKCR4ILLuz3RB8QT7zkadmS2LmFuMwCgweqG
lFm5Fqx/iW5+k0QaQZ3W9mY=
=V3i7
-----END PGP SIGNATURE-----
*/
/*
-----BEGIN PGP SIGNED MESSAGE-----
Hash: SHA1

*/
    /* Complication's key */
    private static final String DEFAULT_TRUSTED_KEY3 =
        "JHFA0yXUgKtmhajXFZH9Nk62OPRHbvvQHTi8EANV-D~3tjLjaz9p9cs6F" +
        "s8W3FSLfUwsQeFg7dfVSQQZga~1jMjboo94vIcm3j6XbW4mbcorVQ74uP" +
        "jd8EA1AQhJ6bBTxDAFk~6fVDOdhHT0Wo5CcUn7v8bAYY3x3UWiL8Remx0=";
/*
-----BEGIN PGP SIGNATURE-----
Version: GnuPG v1.4.6 (GNU/Linux)

iD8DBQFHphOV+h38a3n8zjMRAll+AJ9KA6WiDJcTN4qfrslSemUMr+FBrwCeM8pF
D8usM7Dxp5yrDrCYZ5AIijc=
=SrXI
-----END PGP SIGNATURE-----
*/
/*
-----BEGIN PGP SIGNED MESSAGE-----
Hash: SHA1

*/
    /* HungryHobo's key */
    private static final String DEFAULT_TRUSTED_KEY4 =
        "l3G6um9nB9EDLkT9cUusz5fX-GxXSWE5zaj2~V8lUL~XsGuFf8gKqzJLK" +
        "NkAw0CgDIDsLRHHuUaF7ZHo5Z7HG~9JJU9Il4G2jyNYtg5S8AzG0UxkEt" +
        "-JeBEqIxv5GDn6OFKr~wTI0UafJbegEWokl-8m-GPWf0vW-yPMjL7y5MI=";
/*
-----BEGIN PGP SIGNATURE-----
Version: GnuPG v1.4.10 (GNU/Linux)

iEYEARECAAYFAkysnNIACgkQHix7YXbc3BJVfwCeNGUHaWSqZUbWN9L8VyQLpwxI
JXQAnA28vDmMMMH/WPbC5ixmJeGGNUiR
=3oMC
-----END PGP SIGNATURE-----
*/
/*
-----BEGIN PGP SIGNED MESSAGE-----
Hash: SHA1

*/
    /* KillYourTV's key */
    private static final String DEFAULT_TRUSTED_KEY5 =
        "DAVvT6zMcRuzJi3V8DKKV6o0GjXoQsEwnJsFMaVG1Se-KPQjfP8PbgKJD" +
        "crFe0zNJfh3yPdsocA~A~s9U6pvimlCXH2pnJGlNNojtFCZC3DleROl5-" +
        "4EkYw~UKAg940o5yg1OCBVlRZBSrRAQIIjFGkxxPQc12dA~cfpryNk7Dc=";
/*
-----BEGIN PGP SIGNATURE-----
Version: GnuPG v1.4.11 (GNU/Linux)

iQEcBAEBAgAGBQJO7TSnAAoJEKvgwxnfCgoaJVIIAJbJNdwgqCHkmgPeBEWZbtaM
EkmIL4UC75wVD8yiYReKreX7tJCL7NaeJvnNMNItgy4qJpr+bY0TkJ/LcFoq9ugE
ABBRJD2XDPFjPWYQ0nTiFj3IpWdbxLZAAXXFttyFLDdw52aWUH7nd6TdxFHh1Ssi
pU0yyu77FP5iq3dSTPZUEpA8NB/T6ImbqKQqRltst+TdnbzEwwFB23cihA286cJX
rcoh8CyklYiT3wr46epmHEetseEffxktvn+iCbtRpkA0oLXdVQ0d8cNuB00YUEyB
riCe6OlAEiNpcc6mMyIYYWFICbrDFTrDR3wXqwc/Jkcx6L5VVWoagpSzbo3yGhc=
=8ix/
-----END PGP SIGNATURE-----
*/

    private static final int    VERSION_BYTES       = 16;
    public static final int    HEADER_BYTES        = Signature.SIGNATURE_BYTES + VERSION_BYTES;
    private static final String PROP_TRUSTED_KEYS   = "router.trustedUpdateKeys";

    private final I2PAppContext _context;

    private final Log _log;
    private final Map<SigningPublicKey, String> _trustedKeys;
    private String _newVersion;
    /** 172 */
    private static final int KEYSIZE_B64_BYTES = 2 + (SigningPublicKey.KEYSIZE_BYTES * 4 / 3);

    /**
     * Constructs a new <code>TrustedUpdate</code> with the default global
     * context.
     */
    public TrustedUpdate() {
        this(I2PAppContext.getGlobalContext());
    }

    /**
     * Constructs a new <code>TrustedUpdate</code> with the given
     * {@link net.i2p.I2PAppContext}.
     * 
     * @param context An instance of <code>I2PAppContext</code>.
     */
    public TrustedUpdate(I2PAppContext context) {
        _context = context;
        _log = _context.logManager().getLog(TrustedUpdate.class);
        _trustedKeys = new HashMap(4);

        String propertyTrustedKeys = context.getProperty(PROP_TRUSTED_KEYS);

        if ( (propertyTrustedKeys != null) && (propertyTrustedKeys.length() > 0) ) {
            StringTokenizer propertyTrustedKeysTokens = new StringTokenizer(propertyTrustedKeys, " ,\r\n");

            while (propertyTrustedKeysTokens.hasMoreTokens())
                addKey(propertyTrustedKeysTokens.nextToken().trim(), "");

        } else {
            //addKey(DEFAULT_TRUSTED_KEY, "jrandom@mail.i2p");
            addKey(DEFAULT_TRUSTED_KEY2, "zzz@mail.i2p");
            //addKey(DEFAULT_TRUSTED_KEY3, "complication@mail.i2p");
            addKey(DEFAULT_TRUSTED_KEY4, "HungryHobo@mail.i2p");
            addKey(DEFAULT_TRUSTED_KEY5, "killyourtv@mail.i2p");
        }
        if (_log.shouldLog(Log.DEBUG))
            _log.debug("TrustedUpdate created, trusting " + _trustedKeys.size() + " keys.");
    }

    /**
     *  Duplicate keys or names rejected,
     *  except that duplicate empty names are allowed
     *  @param key 172 character base64 string
     *  @param name non-null but "" ok
     *  @since 0.7.12
     *  @return true if successful
     */
    public boolean addKey(String key, String name) {
        SigningPublicKey signingPublicKey = new SigningPublicKey();
        try {
            // fromBase64() will throw a DFE if length is not right
            signingPublicKey.fromBase64(key);
        } catch (DataFormatException dfe) {
            _log.error("Invalid signing key for " + name + " : " + key, dfe);
            return false;
        }
        String oldName = _trustedKeys.get(signingPublicKey);
        // already there?
        if (name.equals(oldName))
            return true;
        if (oldName != null && !oldName.equals("")) {
            _log.error("Key for " + name + " already stored for different name " + oldName + " : " + key);
            return false;
        }
        if ((!name.equals("")) && _trustedKeys.containsValue(name)) {
            _log.error("Key mismatch for " + name + ", spoof attempt? : " + key);
            return false;
        }
        _trustedKeys.put(signingPublicKey, name);
        return true;
    }

    /**
     *  Do we know about the following key?
     *  @since 0.7.12
     */
    public boolean haveKey(String key) {
        if (key.length() != KEYSIZE_B64_BYTES)
            return false;
        SigningPublicKey signingPublicKey = new SigningPublicKey();
        try {
            signingPublicKey.fromBase64(key);
        } catch (DataFormatException dfe) {
            return false;
        }
        return _trustedKeys.containsKey(signingPublicKey);
    }

    /**
     * Parses command line arguments when this class is used from the command
     * line.
     * Exits 1 on failure so this can be used in scripts.
     * 
     * @param args Command line parameters.
     */
    public static void main(String[] args) {
        boolean ok = false;
        try {
            if ("keygen".equals(args[0])) {
                ok = genKeysCLI(args[1], args[2]);
            } else if ("showversion".equals(args[0])) {
                ok = showVersionCLI(args[1]);
            } else if ("sign".equals(args[0])) {
                ok = signCLI(args[1], args[2], args[3], args[4]);
            } else if ("verifysig".equals(args[0])) {
                ok = verifySigCLI(args[1]);
            } else if ("verifyupdate".equals(args[0])) {
                ok = verifyUpdateCLI(args[1]);
            } else if ("verifyversion".equals(args[0])) {
                ok = verifyVersionCLI(args[1]);
            } else {
                showUsageCLI();
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            showUsageCLI();
        }
        if (!ok)
            System.exit(1);
    }

    /**
     * Checks if the given version is newer than the given current version.
     * 
     * @param currentVersion The current version.
     * @param newVersion     The version to test.
     * 
     * @return <code>true</code> if the given version is newer than the current
     *         version, otherwise <code>false</code>.
     */
    public static final boolean needsUpdate(String currentVersion, String newVersion) {
        return (new VersionComparator()).compare(currentVersion, newVersion) < 0;
    }

    /** @return success */
    private static final boolean genKeysCLI(String publicKeyFile, String privateKeyFile) {
        FileOutputStream fileOutputStream = null;

        I2PAppContext context = I2PAppContext.getGlobalContext();
        try {
            Object signingKeypair[] = context.keyGenerator().generateSigningKeypair();
            SigningPublicKey signingPublicKey = (SigningPublicKey) signingKeypair[0];
            SigningPrivateKey signingPrivateKey = (SigningPrivateKey) signingKeypair[1];

            fileOutputStream = new FileOutputStream(publicKeyFile);
            signingPublicKey.writeBytes(fileOutputStream);
            fileOutputStream.close();
            fileOutputStream = null;

            fileOutputStream = new FileOutputStream(privateKeyFile);
            signingPrivateKey.writeBytes(fileOutputStream);

            System.out.println("\r\nPrivate key written to: " + privateKeyFile);
            System.out.println("Public key written to: " + publicKeyFile);
            System.out.println("\r\nPublic key: " + signingPublicKey.toBase64() + "\r\n");
        } catch (Exception e) {
            System.err.println("Error writing keys:");
            e.printStackTrace();
            return false;
        } finally {
            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException ioe) {
                }
        }
        return true;
    }

    private static final void showUsageCLI() {
        System.err.println("Usage: TrustedUpdate keygen        publicKeyFile privateKeyFile");
        System.err.println("       TrustedUpdate showversion   signedFile");
        System.err.println("       TrustedUpdate sign          inputFile signedFile privateKeyFile version");
        System.err.println("       TrustedUpdate verifysig     signedFile");
        System.err.println("       TrustedUpdate verifyupdate  signedFile");
        System.err.println("       TrustedUpdate verifyversion signedFile");
    }

    /** @return success */
    private static final boolean showVersionCLI(String signedFile) {
        String versionString = getVersionString(new File(signedFile));

        if (versionString.equals(""))
            System.out.println("No version string found in file '" + signedFile + "'");
        else
            System.out.println("Version: " + versionString);
        return !versionString.equals("");
    }

    /** @return success */
    private static final boolean signCLI(String inputFile, String signedFile, String privateKeyFile, String version) {
        Signature signature = new TrustedUpdate().sign(inputFile, signedFile, privateKeyFile, version);

        if (signature != null)
            System.out.println("Input file '" + inputFile + "' signed and written to '" + signedFile + "'");
        else
            System.out.println("Error signing input file '" + inputFile + "'");
        return signature != null;
    }

    /** @return valid */
    private static final boolean verifySigCLI(String signedFile) {
        boolean isValidSignature = new TrustedUpdate().verify(new File(signedFile));

        if (isValidSignature)
            System.out.println("Signature VALID");
        else
            System.out.println("Signature INVALID");
        return isValidSignature;
    }

    /** @return if newer */
    private static final boolean verifyUpdateCLI(String signedFile) {
        boolean isUpdate = new TrustedUpdate().isUpdatedVersion(CoreVersion.VERSION, new File(signedFile));

        if (isUpdate)
            System.out.println("File version is newer than current version.");
        else
            System.out.println("File version is older than or equal to current version.");

        return isUpdate;
    }

    /**
     *  @return true if there's no version mismatch
     *  @since 0.8.8
     */
    private static final boolean verifyVersionCLI(String signedFile) {
        TrustedUpdate tu = new TrustedUpdate();
        File file = new File(signedFile);
        // ignore result, just used to read in version
        tu.isUpdatedVersion("0", file);

        boolean isMatch = tu.verifyVersionMatch(file);
        if (isMatch)
            System.out.println("Version verified");
        else
            System.out.println("Version mismatch, header version does not match zip comment version");

        return isMatch;
    }

    /**
     * Fetches the trusted keys for the current instance.
     * 
     * @return An <code>ArrayList</code> containting the trusted keys.
     */
/***
    public ArrayList getTrustedKeys() {
        return _trustedKeys;
    }
***/    
    
    /**
     * Fetches the trusted keys for the current instance.
     * We could sort it but don't bother.
     * 
     * @return A <code>String</code> containing the trusted keys,
     * delimited by CR LF line breaks.
     */
    public String getTrustedKeysString() {
        StringBuilder buf = new StringBuilder(1024);
        for (SigningPublicKey spk : _trustedKeys.keySet()) {
            // If something already buffered, first add line break.
            if (buf.length() > 0) buf.append("\r\n");
            buf.append(spk.toBase64());
        }
            
        return buf.toString();
    }

    
    /**
     * Reads the version string from a signed update file.
     * 
     * @param signedFile A signed update file.
     * 
     * @return The version string read, or an empty string if no version string
     *         is present.
     */
    public static String getVersionString(File signedFile) {
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(signedFile);
            long skipped = fileInputStream.skip(Signature.SIGNATURE_BYTES);
            if (skipped != Signature.SIGNATURE_BYTES)
                return "";
            byte[] data = new byte[VERSION_BYTES];
            int bytesRead = DataHelper.read(fileInputStream, data);

            if (bytesRead != VERSION_BYTES) {
                return "";
            }

            for (int i = 0; i < VERSION_BYTES; i++) 
                if (data[i] == 0x00) {
                    return new String(data, 0, i, "UTF-8");
                }

            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("wtf, your JVM doesnt support utf-8? " + uee.getMessage());
        } catch (IOException ioe) {
            return "";
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                }
        }
    }
    
    /**
     * Reads the version string from an input stream
     * 
     * @param inputStream containing at least 56 bytes
     * 
     * @return The version string read, or an empty string if no version string
     *         is present.
     *
     * @since 0.7.12
     */
    public static String getVersionString(InputStream inputStream) {
        try {
            long skipped = inputStream.skip(Signature.SIGNATURE_BYTES);
            if (skipped != Signature.SIGNATURE_BYTES)
                return "";
            byte[] data = new byte[VERSION_BYTES];
            int bytesRead = DataHelper.read(inputStream, data);

            if (bytesRead != VERSION_BYTES) {
                return "";
            }

            for (int i = 0; i < VERSION_BYTES; i++) 
                if (data[i] == 0x00) {
                    return new String(data, 0, i, "UTF-8");
                }

            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("wtf, your JVM doesnt support utf-8? " + uee.getMessage());
        } catch (IOException ioe) {
            return "";
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                }
        }
    }

    /** version in the .sud file, valid only after calling migrateVerified() */
    public String newVersion() {
        return _newVersion;
    }

    /**
     * Verifies that the version of the given signed update file is newer than
     * <code>currentVersion</code>.
     * 
     * @param currentVersion The current version to check against.
     * @param signedFile     The signed update file.
     * 
     * @return <code>true</code> if the signed update file's version is newer
     *         than the current version, otherwise <code>false</code>.
     */
    public boolean isUpdatedVersion(String currentVersion, File signedFile) {
        _newVersion = getVersionString(signedFile);
        return needsUpdate(currentVersion, _newVersion);
    }

    /**
     * Verifies the signature of a signed update file, and if it's valid and the
     * file's version is newer than the given current version, migrates the data
     * out of <code>signedFile</code> and into <code>outputFile</code>.
     * 
     * As of 0.8.8, the embedded file must be a zip file with
     * a standard zip header and a UTF-8 zip file comment
     * matching the version in the sud header. This prevents spoofing the version,
     * since the sud signature does NOT cover the version in the header.
     * (We do this for sud/su2 files but not plugin xpi2p files -
     * don't use this method for plugin files)
     * 
     * @param currentVersion The current version to check against.
     * @param signedFile     A signed update file.
     * @param outputFile     The file to write the verified data to.
     * 
     * @return <code>null</code> if the signature and version were valid and the
     *         data was moved, and an error <code>String</code> otherwise.
     */
    public String migrateVerified(String currentVersion, File signedFile, File outputFile) {
        if (!signedFile.exists())
            return "File not found: " + signedFile.getAbsolutePath();
        if (!isUpdatedVersion(currentVersion, signedFile)) {
            if ("".equals(_newVersion))
                return "Truncated or corrupt file: " + signedFile.getAbsolutePath();
            else
                return "Downloaded version is not greater than current version";
        }

        if (!verifyVersionMatch(signedFile))
            return "Update file invalid - signed version mismatch";

        if (!verify(signedFile))
            return "Unknown signing key or corrupt file";

        return migrateFile(signedFile, outputFile);
    }

    /**
     * Verify the version in the sud header matches the version in the zip comment
     * (and that the embedded file is a zip file at all)
     * isUpdatedVersion() must be called first to set _newVersion.
     * 
     * @return true if matches
     *
     * @since 0.8.8
     */
    private boolean verifyVersionMatch(File signedFile) {
        try {
             String zipComment = ZipFileComment.getComment(signedFile, VERSION_BYTES, HEADER_BYTES);
             return zipComment.equals(_newVersion);
        } catch (IOException ioe) {}
        return false;
    }

    /**
     * Extract the file. Skips and ignores the signature and version. No verification.
     * 
     * @param signedFile     A signed update file.
     * @param outputFile     The file to write the verified data to.
     * 
     * @return <code>null</code> if the
     *         data was moved, and an error <code>String</code> otherwise.
     *
     * @since 0.7.12
     */
    public String migrateFile(File signedFile, File outputFile) {
        if (!signedFile.exists())
            return "File not found: " + signedFile.getAbsolutePath();

        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            fileInputStream = new FileInputStream(signedFile);
            fileOutputStream = new FileOutputStream(outputFile);
            long skipped = 0;

            while (skipped < HEADER_BYTES)
                skipped += fileInputStream.skip(HEADER_BYTES - skipped);

            byte[] buffer = new byte[16*1024];
            int bytesRead = 0;

            while ( (bytesRead = fileInputStream.read(buffer)) != -1) 
                fileOutputStream.write(buffer, 0, bytesRead);
        } catch (IOException ioe) {
            // probably permissions or disk full, so bring the message out to the console
            return "Error copying update: " + ioe;
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                }

            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException ioe) {
                }
        }

        return null;
    }

    /**
     * Uses the given private key to sign the given input file along with its
     * version string using DSA. The output will be a signed update file where
     * the first 40 bytes are the resulting DSA signature, the next 16 bytes are
     * the input file's version string encoded in UTF-8 (padded with trailing
     * <code>0h</code> characters if necessary), and the remaining bytes are the
     * raw bytes of the input file.
     * 
     * @param inputFile      The file to be signed.
     * @param signedFile     The signed update file to write.
     * @param privateKeyFile The name of the file containing the private key to
     *                       sign <code>inputFile</code> with.
     * @param version        The version string of the input file. If this is
     *                       longer than 16 characters it will be truncated.
     * 
     * @return An instance of {@link net.i2p.data.Signature}, or
     *         <code>null</code> if there was an error.
     */
    public Signature sign(String inputFile, String signedFile, String privateKeyFile, String version) {
        FileInputStream fileInputStream = null;
        SigningPrivateKey signingPrivateKey = new SigningPrivateKey();

        try {
            fileInputStream = new FileInputStream(privateKeyFile);
            signingPrivateKey.readBytes(fileInputStream);
        } catch (IOException ioe) {
            if (_log.shouldLog(Log.WARN))
                _log.warn("Unable to load the signing key", ioe);

            return null;
        } catch (DataFormatException dfe) {
            if (_log.shouldLog(Log.WARN))
                _log.warn("Unable to load the signing key", dfe);

            return null;
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                }
        }

        return sign(inputFile, signedFile, signingPrivateKey, version);
    }

    /**
     * Uses the given {@link net.i2p.data.SigningPrivateKey} to sign the given
     * input file along with its version string using DSA. The output will be a
     * signed update file where the first 40 bytes are the resulting DSA
     * signature, the next 16 bytes are the input file's version string encoded
     * in UTF-8 (padded with trailing <code>0h</code> characters if necessary),
     * and the remaining bytes are the raw bytes of the input file.
     * 
     * @param inputFile         The file to be signed.
     * @param signedFile        The signed update file to write.
     * @param signingPrivateKey An instance of <code>SigningPrivateKey</code>
     *                          to sign <code>inputFile</code> with.
     * @param version           The version string of the input file. If this is
     *                          longer than 16 characters it will be truncated.
     * 
     * @return An instance of {@link net.i2p.data.Signature}, or
     *         <code>null</code> if there was an error.
     */
    public Signature sign(String inputFile, String signedFile, SigningPrivateKey signingPrivateKey, String version) {
        byte[] versionHeader = {
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00 };
        byte[] versionRawBytes = null;

        if (version.length() > VERSION_BYTES)
            version = version.substring(0, VERSION_BYTES);

        try {
            versionRawBytes = version.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("wtf, your JVM doesnt support utf-8? " + e.getMessage());
        }

        System.arraycopy(versionRawBytes, 0, versionHeader, 0, versionRawBytes.length);

        FileInputStream fileInputStream = null;
        Signature signature = null;
        SequenceInputStream bytesToSignInputStream = null;
        ByteArrayInputStream versionHeaderInputStream = null;

        try {
            fileInputStream = new FileInputStream(inputFile);
            versionHeaderInputStream = new ByteArrayInputStream(versionHeader);
            bytesToSignInputStream = new SequenceInputStream(versionHeaderInputStream, fileInputStream);
            signature = _context.dsa().sign(bytesToSignInputStream, signingPrivateKey);

        } catch (Exception e) {
            if (_log.shouldLog(Log.ERROR))
                _log.error("Error signing", e);

            return null;
        } finally {
            if (bytesToSignInputStream != null)
                try {
                    bytesToSignInputStream.close();
                } catch (IOException ioe) {
                }

            fileInputStream = null;
        }

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(signedFile);
            fileOutputStream.write(signature.getData());
            fileOutputStream.write(versionHeader);
            fileInputStream = new FileInputStream(inputFile);
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ( (bytesRead = fileInputStream.read(buffer)) != -1) 
                fileOutputStream.write(buffer, 0, bytesRead);
            fileOutputStream.close();
        } catch (IOException ioe) {
            if (_log.shouldLog(Log.WARN))
                _log.log(Log.WARN, "Error writing signed file " + signedFile, ioe);

            return null;
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                }

            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException ioe) {
                }
        }

        return signature;
    }

    /**
     * Verifies the DSA signature of a signed update file.
     * 
     * @param signedFile The signed update file to check.
     * 
     * @return <code>true</code> if the file has a valid signature, otherwise
     *         <code>false</code>.
     */
    public boolean verify(File signedFile) {
        for (SigningPublicKey signingPublicKey : _trustedKeys.keySet()) {
            boolean isValidSignature = verify(signedFile, signingPublicKey);
            if (isValidSignature)
                return true;
        }

        if (_log.shouldLog(Log.WARN))
            _log.warn("None of the keys match");

        return false;
    }

    /**
     * Verifies the DSA signature of a signed update file.
     * 
     * @param signedFile The signed update file to check.
     * 
     * @return signer (could be empty string) or null if invalid
     * @since 0.7.12
     */
    public String verifyAndGetSigner(File signedFile) {
        for (SigningPublicKey signingPublicKey : _trustedKeys.keySet()) {
            boolean isValidSignature = verify(signedFile, signingPublicKey);
            if (isValidSignature)
                return _trustedKeys.get(signingPublicKey);
        }
        return null;
    }

    /**
     * Verifies the DSA signature of a signed update file.
     * 
     * @param signedFile    The signed update file to check.
     * @param publicKeyFile A file containing the public key to use for
     *                      verification.
     * 
     * @return <code>true</code> if the file has a valid signature, otherwise
     *         <code>false</code>.
     */
    public boolean verify(String signedFile, String publicKeyFile) {
        SigningPublicKey signingPublicKey = new SigningPublicKey();
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(signedFile);
            signingPublicKey.readBytes(fileInputStream);
        } catch (IOException ioe) {
            if (_log.shouldLog(Log.WARN))
                _log.warn("Unable to load the signature", ioe);

            return false;
        } catch (DataFormatException dfe) {
            if (_log.shouldLog(Log.WARN))
                _log.warn("Unable to load the signature", dfe);

            return false;
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                }
        }

        return verify(new File(signedFile), signingPublicKey);
    }

    /**
     * Verifies the DSA signature of a signed update file.
     * 
     * @param signedFile       The signed update file to check.
     * @param signingPublicKey An instance of
     *                         {@link net.i2p.data.SigningPublicKey} to use for
     *                         verification.
     * 
     * @return <code>true</code> if the file has a valid signature, otherwise
     *         <code>false</code>.
     */
    public boolean verify(File signedFile, SigningPublicKey signingPublicKey) {
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(signedFile);
            Signature signature = new Signature();

            signature.readBytes(fileInputStream);

            return _context.dsa().verifySignature(signature, fileInputStream, signingPublicKey);
        } catch (IOException ioe) {
            if (_log.shouldLog(Log.WARN))
                _log.warn("Error reading " + signedFile + " to verify", ioe);

            return false;
        } catch (DataFormatException dfe) {
            if (_log.shouldLog(Log.ERROR))
                _log.error("Error reading the signature", dfe);

            return false;
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException ioe) {
                }
        }
    }
}
