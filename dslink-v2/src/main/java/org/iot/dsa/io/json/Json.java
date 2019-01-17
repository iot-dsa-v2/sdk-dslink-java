package org.iot.dsa.io.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSString;
import org.iot.dsa.util.DSException;

/**
 * Convenience routines for JSON.  All routines use UTF-8 encoding.
 */
public class Json {

    /**
     * Returns the first element from the sequence.
     */
    public static DSElement read(CharSequence string) {
        JsonReader in = null;
        DSElement ret = null;
        try {
            in = new JsonReader(string);
            ret = in.getElement();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception whoCares) {
                }
            }
        }
        return ret;
    }

    /**
     * Returns the first element in the file.
     */
    public static DSElement read(File file) {
        JsonReader in = null;
        DSElement ret = null;
        try {
            in = new JsonReader(new InputStreamReader(new FileInputStream(file), DSString.UTF8));
            ret = in.getElement();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception whoCares) {
                }
            }
        }
        return ret;
    }

    /**
     * Returns the first element in the stream and optionally closes it.
     */
    public static DSElement read(InputStream input, boolean close) {
        JsonReader in = null;
        DSElement ret = null;
        try {
            in = new JsonReader(new InputStreamReader(input, DSString.UTF8));
            ret = in.getElement();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        } finally {
            if (close && (in != null)) {
                try {
                    in.close();
                } catch (Exception whoCares) {
                }
            }
        }
        return ret;
    }

    /**
     * Returns the first element in the stream and optionally closes it.
     */
    public static DSElement read(Reader reader, boolean close) {
        JsonReader in = null;
        DSElement ret = null;
        try {
            in = new JsonReader(reader);
            ret = in.getElement();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        } finally {
            if (close && (in != null)) {
                try {
                    in.close();
                } catch (Exception whoCares) {
                }
            }
        }
        return ret;
    }

    /**
     * Returns a JSON reader.
     */
    public static DSIReader reader(CharSequence in) {
        return new JsonReader(in);
    }

    /**
     * Returns a JSON reader.
     */
    public static DSIReader reader(File in) {
        return new JsonReader(in);
    }

    /**
     * Returns a JSON reader.
     */
    public static DSIReader reader(InputStream in) {
        return new JsonReader(in);
    }

    /**
     * It is the callers responsibility to determine the charset.
     */
    public static DSIReader reader(Reader in) {
        return new JsonReader(in);
    }

    /**
     * Write the data to the file without pretty print.
     */
    public static void write(DSElement data, File file) {
        write(data, file, false);
    }

    /**
     * Overwrites the file, pretty printing is optional.
     */
    public static void write(DSElement data, File file, boolean prettyPrint) {
        try {
            write(data, new FileOutputStream(file), true, prettyPrint);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    /**
     * Write the data to the file, pretty printing is optional.
     *
     * @return The OutputStream argument.
     */
    public static OutputStream write(DSElement data, OutputStream out, boolean close) {
        return write(data, out, close, false);
    }

    /**
     * Write the data to the file, pretty printing is optional.
     *
     * @return The OutputStream argument.
     */
    public static OutputStream write(DSElement data,
                                     OutputStream out,
                                     boolean close,
                                     boolean prettyPrint) {
        JsonWriter writer = null;
        try {
            writer = new JsonWriter(out);
            writer.setPrettyPrint(prettyPrint);
            writer.value(data);
            writer.flush();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        } finally {
            if (close && (writer != null)) {
                try {
                    writer.close();
                } catch (Exception whocares) {
                }
            }
        }
        return out;
    }

    /**
     * Returns a JSON writer.
     */
    public static DSIWriter writer(Appendable out) {
        if (out instanceof Writer) {
            return new JsonWriter((Writer) out);
        }
        return new JsonAppender(out);
    }

    /**
     * Returns a JSON writer.
     */
    public static DSIWriter writer(File out) {
        return new JsonWriter(out);
    }

    /**
     * Returns a JSON writer.
     */
    public static DSIWriter writer(OutputStream out) {
        return new JsonWriter(out);
    }

    /**
     * Returns a JSON writer.
     *
     * @param out           The file to overwrite.
     * @param innerFileName The name of the json file inside the zip.
     */
    public static DSIWriter zipWriter(File out, String innerFileName) {
        return new JsonWriter(out, innerFileName);
    }

}
