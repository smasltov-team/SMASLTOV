// @author Tal Lev-Ami.
// @since 9.5.2001 Added the characters '$' and '.' to identifiers (Roman).
package tvla.jeannet.language.TVP;
import tvla.exceptions.*;
import tvla.util.*;
import java_cup.runtime.Symbol;
import java.util.*;


class TVPLex implements java_cup.runtime.Scanner {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

int line_count = 1;
void addProperties(String props) {
	try {
		int b = props.indexOf('\"')+1;
		int e = props.indexOf('\"', b);
		String filename = props.substring(b, e);
		ProgramProperties.load(filename);
	}
	catch (Exception e) {
		throw new UserErrorException("Unable to load properties file specified by " +
		props);
	}
}
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private boolean yy_at_bol;
	private int yy_lexical_state;

	TVPLex (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	TVPLex (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private TVPLex () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int YYINITIAL = 0;
	private final int COMMENT = 1;
	private final int LINECOMMENT = 2;
	private final int yy_state_dtrans[] = {
		0,
		115,
		141
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NO_ANCHOR,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NO_ANCHOR,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NO_ANCHOR,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NO_ANCHOR,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NO_ANCHOR,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NO_ANCHOR,
		/* 58 */ YY_NO_ANCHOR,
		/* 59 */ YY_NO_ANCHOR,
		/* 60 */ YY_NO_ANCHOR,
		/* 61 */ YY_NO_ANCHOR,
		/* 62 */ YY_NO_ANCHOR,
		/* 63 */ YY_NO_ANCHOR,
		/* 64 */ YY_NO_ANCHOR,
		/* 65 */ YY_NO_ANCHOR,
		/* 66 */ YY_NO_ANCHOR,
		/* 67 */ YY_NO_ANCHOR,
		/* 68 */ YY_NO_ANCHOR,
		/* 69 */ YY_NO_ANCHOR,
		/* 70 */ YY_NO_ANCHOR,
		/* 71 */ YY_NO_ANCHOR,
		/* 72 */ YY_NO_ANCHOR,
		/* 73 */ YY_NO_ANCHOR,
		/* 74 */ YY_NO_ANCHOR,
		/* 75 */ YY_NO_ANCHOR,
		/* 76 */ YY_NO_ANCHOR,
		/* 77 */ YY_NO_ANCHOR,
		/* 78 */ YY_NO_ANCHOR,
		/* 79 */ YY_NO_ANCHOR,
		/* 80 */ YY_NO_ANCHOR,
		/* 81 */ YY_NO_ANCHOR,
		/* 82 */ YY_NOT_ACCEPT,
		/* 83 */ YY_NO_ANCHOR,
		/* 84 */ YY_NO_ANCHOR,
		/* 85 */ YY_NO_ANCHOR,
		/* 86 */ YY_NO_ANCHOR,
		/* 87 */ YY_NOT_ACCEPT,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NO_ANCHOR,
		/* 91 */ YY_NOT_ACCEPT,
		/* 92 */ YY_NO_ANCHOR,
		/* 93 */ YY_NOT_ACCEPT,
		/* 94 */ YY_NO_ANCHOR,
		/* 95 */ YY_NOT_ACCEPT,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NOT_ACCEPT,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NOT_ACCEPT,
		/* 100 */ YY_NO_ANCHOR,
		/* 101 */ YY_NOT_ACCEPT,
		/* 102 */ YY_NO_ANCHOR,
		/* 103 */ YY_NOT_ACCEPT,
		/* 104 */ YY_NO_ANCHOR,
		/* 105 */ YY_NOT_ACCEPT,
		/* 106 */ YY_NO_ANCHOR,
		/* 107 */ YY_NOT_ACCEPT,
		/* 108 */ YY_NO_ANCHOR,
		/* 109 */ YY_NOT_ACCEPT,
		/* 110 */ YY_NO_ANCHOR,
		/* 111 */ YY_NOT_ACCEPT,
		/* 112 */ YY_NO_ANCHOR,
		/* 113 */ YY_NOT_ACCEPT,
		/* 114 */ YY_NO_ANCHOR,
		/* 115 */ YY_NOT_ACCEPT,
		/* 116 */ YY_NO_ANCHOR,
		/* 117 */ YY_NOT_ACCEPT,
		/* 118 */ YY_NO_ANCHOR,
		/* 119 */ YY_NOT_ACCEPT,
		/* 120 */ YY_NO_ANCHOR,
		/* 121 */ YY_NOT_ACCEPT,
		/* 122 */ YY_NO_ANCHOR,
		/* 123 */ YY_NOT_ACCEPT,
		/* 124 */ YY_NO_ANCHOR,
		/* 125 */ YY_NOT_ACCEPT,
		/* 126 */ YY_NO_ANCHOR,
		/* 127 */ YY_NOT_ACCEPT,
		/* 128 */ YY_NO_ANCHOR,
		/* 129 */ YY_NOT_ACCEPT,
		/* 130 */ YY_NO_ANCHOR,
		/* 131 */ YY_NOT_ACCEPT,
		/* 132 */ YY_NO_ANCHOR,
		/* 133 */ YY_NOT_ACCEPT,
		/* 134 */ YY_NO_ANCHOR,
		/* 135 */ YY_NOT_ACCEPT,
		/* 136 */ YY_NO_ANCHOR,
		/* 137 */ YY_NOT_ACCEPT,
		/* 138 */ YY_NO_ANCHOR,
		/* 139 */ YY_NOT_ACCEPT,
		/* 140 */ YY_NO_ANCHOR,
		/* 141 */ YY_NOT_ACCEPT,
		/* 142 */ YY_NO_ANCHOR,
		/* 143 */ YY_NOT_ACCEPT,
		/* 144 */ YY_NOT_ACCEPT,
		/* 145 */ YY_NOT_ACCEPT,
		/* 146 */ YY_NOT_ACCEPT,
		/* 147 */ YY_NOT_ACCEPT,
		/* 148 */ YY_NOT_ACCEPT,
		/* 149 */ YY_NOT_ACCEPT,
		/* 150 */ YY_NOT_ACCEPT,
		/* 151 */ YY_NOT_ACCEPT,
		/* 152 */ YY_NOT_ACCEPT,
		/* 153 */ YY_NOT_ACCEPT,
		/* 154 */ YY_NOT_ACCEPT,
		/* 155 */ YY_NO_ANCHOR,
		/* 156 */ YY_NO_ANCHOR,
		/* 157 */ YY_NO_ANCHOR,
		/* 158 */ YY_NO_ANCHOR,
		/* 159 */ YY_NO_ANCHOR,
		/* 160 */ YY_NO_ANCHOR,
		/* 161 */ YY_NO_ANCHOR,
		/* 162 */ YY_NO_ANCHOR,
		/* 163 */ YY_NO_ANCHOR,
		/* 164 */ YY_NO_ANCHOR,
		/* 165 */ YY_NO_ANCHOR,
		/* 166 */ YY_NO_ANCHOR,
		/* 167 */ YY_NO_ANCHOR,
		/* 168 */ YY_NO_ANCHOR,
		/* 169 */ YY_NO_ANCHOR,
		/* 170 */ YY_NO_ANCHOR,
		/* 171 */ YY_NO_ANCHOR,
		/* 172 */ YY_NO_ANCHOR,
		/* 173 */ YY_NO_ANCHOR,
		/* 174 */ YY_NO_ANCHOR,
		/* 175 */ YY_NO_ANCHOR,
		/* 176 */ YY_NO_ANCHOR,
		/* 177 */ YY_NO_ANCHOR,
		/* 178 */ YY_NO_ANCHOR,
		/* 179 */ YY_NO_ANCHOR,
		/* 180 */ YY_NO_ANCHOR,
		/* 181 */ YY_NO_ANCHOR,
		/* 182 */ YY_NO_ANCHOR,
		/* 183 */ YY_NO_ANCHOR,
		/* 184 */ YY_NO_ANCHOR,
		/* 185 */ YY_NO_ANCHOR,
		/* 186 */ YY_NO_ANCHOR,
		/* 187 */ YY_NO_ANCHOR,
		/* 188 */ YY_NO_ANCHOR,
		/* 189 */ YY_NO_ANCHOR,
		/* 190 */ YY_NO_ANCHOR,
		/* 191 */ YY_NO_ANCHOR,
		/* 192 */ YY_NO_ANCHOR,
		/* 193 */ YY_NO_ANCHOR,
		/* 194 */ YY_NO_ANCHOR,
		/* 195 */ YY_NO_ANCHOR,
		/* 196 */ YY_NO_ANCHOR,
		/* 197 */ YY_NO_ANCHOR,
		/* 198 */ YY_NO_ANCHOR,
		/* 199 */ YY_NO_ANCHOR,
		/* 200 */ YY_NO_ANCHOR,
		/* 201 */ YY_NO_ANCHOR,
		/* 202 */ YY_NO_ANCHOR,
		/* 203 */ YY_NO_ANCHOR,
		/* 204 */ YY_NO_ANCHOR,
		/* 205 */ YY_NO_ANCHOR,
		/* 206 */ YY_NO_ANCHOR,
		/* 207 */ YY_NO_ANCHOR,
		/* 208 */ YY_NO_ANCHOR,
		/* 209 */ YY_NO_ANCHOR,
		/* 210 */ YY_NO_ANCHOR,
		/* 211 */ YY_NO_ANCHOR,
		/* 212 */ YY_NO_ANCHOR,
		/* 213 */ YY_NO_ANCHOR,
		/* 214 */ YY_NO_ANCHOR,
		/* 215 */ YY_NO_ANCHOR,
		/* 216 */ YY_NO_ANCHOR,
		/* 217 */ YY_NO_ANCHOR,
		/* 218 */ YY_NO_ANCHOR,
		/* 219 */ YY_NO_ANCHOR,
		/* 220 */ YY_NO_ANCHOR,
		/* 221 */ YY_NO_ANCHOR,
		/* 222 */ YY_NO_ANCHOR,
		/* 223 */ YY_NO_ANCHOR,
		/* 224 */ YY_NO_ANCHOR,
		/* 225 */ YY_NO_ANCHOR,
		/* 226 */ YY_NO_ANCHOR,
		/* 227 */ YY_NO_ANCHOR,
		/* 228 */ YY_NO_ANCHOR,
		/* 229 */ YY_NO_ANCHOR,
		/* 230 */ YY_NO_ANCHOR,
		/* 231 */ YY_NO_ANCHOR,
		/* 232 */ YY_NO_ANCHOR,
		/* 233 */ YY_NO_ANCHOR,
		/* 234 */ YY_NO_ANCHOR,
		/* 235 */ YY_NO_ANCHOR,
		/* 236 */ YY_NO_ANCHOR,
		/* 237 */ YY_NO_ANCHOR,
		/* 238 */ YY_NO_ANCHOR,
		/* 239 */ YY_NO_ANCHOR,
		/* 240 */ YY_NO_ANCHOR,
		/* 241 */ YY_NO_ANCHOR,
		/* 242 */ YY_NO_ANCHOR,
		/* 243 */ YY_NO_ANCHOR,
		/* 244 */ YY_NO_ANCHOR,
		/* 245 */ YY_NO_ANCHOR,
		/* 246 */ YY_NO_ANCHOR,
		/* 247 */ YY_NO_ANCHOR,
		/* 248 */ YY_NO_ANCHOR,
		/* 249 */ YY_NO_ANCHOR,
		/* 250 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,130,
"46:9,41,60,46:2,59,46:12,61,46:5,35,39,45,46,49,5,38,46,52,53,2,40,57,36,48" +
",1,44,42,43,48:7,20,58,34,3,4,56,62,31,47,33,47,30,47:14,32,47:6,50,46,51,4" +
"6,27,46,22,16,14,19,17,11,18,21,7,26,47,23,24,13,15,6,47,8,10,9,12,28,25,47" +
",29,47,54,37,55,46:2,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,251,
"0,1,2,1,3,1,4,5,1,6:2,1,7,1:2,8,1,9,1:15,10,1:5,6:2,1:4,6:2,1:4,6:3,11,6:2," +
"1,6:2,1:2,6,1:2,6:2,1,6:6,1:8,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26," +
"27,28,18,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50," +
"51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75," +
"76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100" +
",101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,11" +
"9,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,6,137" +
",138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,15" +
"6,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,6,174" +
",175,176,177")[0];

	private int yy_nxt[][] = unpackFromString(178,63,
"1,2,3,4,5,6,7,84,241,245,246,205,247,181,182,246,183,155,246,248,8,206,184," +
"156,249,246,185,246:3,9,10,89,246,83,11,12,13,14,15,16,11,17,5,18,88,5,246," +
"5,246,19,20,21,22,23,24,25,26,27,28,29,30,5,-1:64,31,32,-1:63,33,-1:65,34,3" +
"5,36,37,38,82,-1:57,246:2,207,246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1" +
":19,246:14,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:17,41,-1:30,93,95,-1:29,4" +
"2,-1:60,97,-1:65,44,-1:64,246:5,250,246:8,-1,246:4,197,246:8,-1:8,208:3,-1:" +
"2,246,208:2,-1:25,101,-1:85,87,91,-1:32,246:7,39,246:6,-1,246:13,-1:8,208:3" +
",-1:2,246,208:2,-1:14,77,-1:67,143,-1:92,91,-1:27,99:44,43,99:17,-1:6,246:1" +
"4,-1,246:12,40,-1:8,208:3,-1:2,246,208:2,-1:19,117,-1:60,47,-1:30,103,-1:33" +
",246:13,45,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:17,41,-1:64,246:3,46,246:" +
"10,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:17,48,-1:64,246:14,50,246:13,-1:8" +
",208:3,-1:2,246,208:2,-1:56,49,-1:25,246:14,-1,246:8,51,246:4,-1:8,208:3,-1" +
":2,246,208:2,-1:19,246:2,52,246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:2" +
"6,105,-1:55,246:9,53,246:4,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:17,47,-1:" +
"64,246:7,54,246:6,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:27,107,-1:54,246:4" +
",55,246:9,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:22,109,-1:59,246:7,56,246:" +
"6,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:20,111,-1:61,246:14,57,246:13,-1:8" +
",208:3,-1:2,246,208:2,-1:28,113,-1:53,246:10,58,246:3,-1,246:13,-1:8,208:3," +
"-1:2,246,208:2,-1:26,67,-1:55,246:11,59,246:2,-1,246:13,-1:8,208:3,-1:2,246" +
",208:2,-1:13,1,75,85,75:56,-1,76,75,90,-1:6,246:14,60,246:13,-1:8,208:3,-1:" +
"2,246,208:2,-1:21,119,-1:60,246:14,61,246:13,-1:8,208:3,-1:2,246,208:2,-1:2" +
"8,121,-1:53,246:14,-1,62,246:12,-1:8,208:3,-1:2,246,208:2,-1:19,123,-1:62,2" +
"46:14,63,246:13,-1:8,208:3,-1:2,246,208:2,-1:30,125,-1:51,246:14,64,246:13," +
"-1:8,208:3,-1:2,246,208:2,-1:21,127,-1:60,246:14,-1,65,246:12,-1:8,208:3,-1" +
":2,246,208:2,-1:22,129,-1:59,246:14,-1,66,246:12,-1:8,208:3,-1:2,246,208:2," +
"-1:20,131,-1:61,246:14,-1,246:3,68,246:9,-1:8,208:3,-1:2,246,208:2,-1:30,13" +
"3,-1:51,246:2,69,246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:23,135,-1:58" +
",246:2,70,246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:48,137,-1:5,137,-1:" +
"27,246:2,71,246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:48,137,-1:5,137,-" +
"1:3,139,-1:23,246:14,-1,72,246:12,-1:8,208:3,-1:2,246,208:2,-1:14,139:44,78" +
",139:17,-1:6,246:14,-1,73,246:12,-1:8,208:3,-1:2,246,208:2,-1:13,1,79:58,-1" +
",80,79,86,-1:6,246:14,74,246:13,-1:8,208:3,-1:2,246,208:2,-1:21,144,-1:69,1" +
"45,-1:53,146,-1:73,147,-1:53,148,-1:63,149,-1:60,150,-1:72,151,-1:55,152,-1" +
":87,153,-1:5,153,-1:56,153,-1:5,153,-1:3,154,-1:18,154:44,81,154:17,-1:6,24" +
"6:7,92,246:6,-1,246:3,213,246:9,-1:8,208:3,-1:2,246,208:2,-1:19,246:11,94,2" +
"46:2,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,246:4,96,246:8,-1:" +
"8,208:3,-1:2,246,208:2,-1:19,98,246:10,190,246:2,-1,246:13,-1:8,208:3,-1:2," +
"246,208:2,-1:19,246:6,100,246:7,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,2" +
"46:3,102,246:10,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246,104,246:12,-1" +
",246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:6,106,246:7,-1,246:13,-1:8,208:" +
"3,-1:2,246,208:2,-1:19,246,108,246:12,-1,246:13,-1:8,208:3,-1:2,246,208:2,-" +
"1:19,246:3,110,246:10,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,2" +
"46,112,246:11,-1:8,208:3,-1:2,246,208:2,-1:19,246:8,114,246:5,-1,246:13,-1:" +
"8,208:3,-1:2,246,208:2,-1:19,246:11,116,246:2,-1,246:13,-1:8,208:3,-1:2,246" +
",208:2,-1:19,246:7,118,246:6,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:" +
"8,120,246:5,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:11,122,246:2,-1,2" +
"46:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:11,124,246:2,-1,246:13,-1:8,208:3" +
",-1:2,246,208:2,-1:19,246:3,126,246:10,-1,246:13,-1:8,208:3,-1:2,246,208:2," +
"-1:19,246:3,128,246:10,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:2,130," +
"246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:6,132,246:7,-1,246:13," +
"-1:8,208:3,-1:2,246,208:2,-1:19,246:11,134,246:2,-1,246:13,-1:8,208:3,-1:2," +
"246,208:2,-1:19,246:11,136,246:2,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19," +
"246:8,138,246:5,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:8,140,246:5,-" +
"1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:7,142,246:6,-1,246:13,-1:8,208" +
":3,-1:2,246,208:2,-1:19,246:11,157,246:2,-1,246:13,-1:8,208:3,-1:2,246,208:" +
"2,-1:19,246:9,158,246:4,-1,246:2,212,246:10,-1:8,208:3,-1:2,246,208:2,-1:19" +
",246:11,187,246:2,-1,246:2,159,246:10,-1:8,208:3,-1:2,246,208:2,-1:19,246:6" +
",160,246:7,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:9,161,246:4,-1,246" +
":13,-1:8,208:3,-1:2,246,208:2,-1:19,246:2,218,246:5,162,246:5,-1,246:13,-1:" +
"8,208:3,-1:2,246,208:2,-1:19,246:12,163,246,-1,246:13,-1:8,208:3,-1:2,246,2" +
"08:2,-1:19,246:14,-1,246:2,164,246:10,-1:8,208:3,-1:2,246,208:2,-1:19,246:5" +
",165,246:2,223,246:5,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:2,166,24" +
"6:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:7,167,246:6,-1,246:13,-1" +
":8,208:3,-1:2,246,208:2,-1:19,246,168,246:12,-1,246:13,-1:8,208:3,-1:2,246," +
"208:2,-1:19,246:14,-1,246,169,246:11,-1:8,208:3,-1:2,246,208:2,-1:19,246:3," +
"170,246:10,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:12,171,246,-1,246:" +
"13,-1:8,208:3,-1:2,246,208:2,-1:19,246,172,246:12,-1,246:13,-1:8,208:3,-1:2" +
",246,208:2,-1:19,246,173,246:12,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,2" +
"46:9,174,246:4,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,246:2,17" +
"5,246:10,-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,246:7,176,246:5,-1:8,208" +
":3,-1:2,246,208:2,-1:19,246:13,177,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:1" +
"9,246:14,-1,246,178,246:11,-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,246,17" +
"9,246:11,-1:8,208:3,-1:2,246,208:2,-1:19,246:9,180,246:4,-1,246:13,-1:8,208" +
":3,-1:2,246,208:2,-1:19,246:9,186,246:4,-1,246:13,-1:8,208:3,-1:2,246,208:2" +
",-1:19,246:14,-1,246,188,246:11,-1:8,208:3,-1:2,246,208:2,-1:19,246:11,189," +
"246:2,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:3,216,246:10,-1,246:13," +
"-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,246,217,246:11,-1:8,208:3,-1:2,24" +
"6,208:2,-1:19,246:13,219,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:9,19" +
"1,246:4,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:10,220,246:3,-1,246:1" +
"3,-1:8,208:3,-1:2,246,208:2,-1:19,246:5,242,246:8,-1,246:13,-1:8,208:3,-1:2" +
",246,208:2,-1:19,246:4,221,246:6,222,246:2,-1,246:13,-1:8,208:3,-1:2,246,20" +
"8:2,-1:19,246:14,-1,246,192,246:11,-1:8,208:3,-1:2,246,208:2,-1:19,246:7,24" +
"3,246:6,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:11,193,246:2,-1,246:1" +
"3,-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,246,194,246:11,-1:8,208:3,-1:2," +
"246,208:2,-1:19,246:11,224,246:2,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19," +
"246:4,226,246:9,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:3,227,246:10," +
"-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:9,228,246:4,-1,246:13,-1:8,20" +
"8:3,-1:2,246,208:2,-1:19,246:13,230,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:" +
"19,246:14,-1,246:6,231,246:6,-1:8,208:3,-1:2,246,208:2,-1:19,246:14,-1,246," +
"195,246:11,-1:8,208:3,-1:2,246,208:2,-1:19,246:5,232,246:8,-1,246:4,196,246" +
":8,-1:8,208:3,-1:2,246,208:2,-1:19,246:7,233,246:6,-1,246:13,-1:8,208:3,-1:" +
"2,246,208:2,-1:19,246:5,198,246:8,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19" +
",246:10,199,246:3,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:6,234,246:2" +
",200,246:4,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:9,235,246:4,-1,246" +
":13,-1:8,208:3,-1:2,246,208:2,-1:19,246:13,236,-1,246:13,-1:8,208:3,-1:2,24" +
"6,208:2,-1:19,246:7,201,246:6,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246" +
":2,237,246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246,239,246:12,-1,2" +
"46:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:11,202,246:2,-1,246:13,-1:8,208:3" +
",-1:2,246,208:2,-1:19,246:11,203,246:2,-1,246:13,-1:8,208:3,-1:2,246,208:2," +
"-1:19,246:3,240,246:10,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246,204,24" +
"6:12,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:11,209,246:2,-1,246:13,-" +
"1:8,208:3,-1:2,246,208:2,-1:19,246:5,225,246:8,-1,246:13,-1:8,208:3,-1:2,24" +
"6,208:2,-1:19,246:4,229,246:9,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246" +
":2,238,246:11,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:2,210,246:11,-1" +
",246:13,-1:8,208:3,-1:2,246,208:2,-1:19,211,246:13,-1,246:13,-1:8,208:3,-1:" +
"2,246,208:2,-1:19,246,214,246:12,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19," +
"246:11,215,246:2,-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:19,246:9,244,246:4," +
"-1,246:13,-1:8,208:3,-1:2,246,208:2,-1:13");

	public Symbol next_token ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

return (new Symbol(sym.EOF));
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:
						
					case -2:
						break;
					case 2:
						{return new Symbol(sym.COMBINE);}
					case -3:
						break;
					case 3:
						{return new Symbol(sym.STAR);}
					case -4:
						break;
					case 4:
						{return new Symbol(sym.ASSIGN);}
					case -5:
						break;
					case 5:
						{ Logger.println("Illegal character: "+yytext()); }
					case -6:
						break;
					case 6:
						{return new Symbol(sym.PERCENT);}
					case -7:
						break;
					case 7:
						{return new Symbol(sym.ID, yytext()); }
					case -8:
						break;
					case 8:
						{return new Symbol(sym.COLON);}
					case -9:
						break;
					case 9:
						{return new Symbol(sym.EXISTS);}
					case -10:
						break;
					case 10:
						{return new Symbol(sym.FORALL);}
					case -11:
						break;
					case 11:
						{}
					case -12:
						break;
					case 12:
						{return new Symbol(sym.MINUS);}
					case -13:
						break;
					case 13:
						{return new Symbol(sym.OR);}
					case -14:
						break;
					case 14:
						{return new Symbol(sym.AND);}
					case -15:
						break;
					case 15:
						{return new Symbol(sym.NOT);}
					case -16:
						break;
					case 16:
						{return new Symbol(sym.PLUS);}
					case -17:
						break;
					case 17:
						{return new Symbol(sym.TRUE);}
					case -18:
						break;
					case 18:
						{return new Symbol(sym.FALSE);}
					case -19:
						break;
					case 19:
						{return new Symbol(sym.LBR); }
					case -20:
						break;
					case 20:
						{return new Symbol(sym.RBR); }
					case -21:
						break;
					case 21:
						{return new Symbol(sym.LP); }
					case -22:
						break;
					case 22:
						{return new Symbol(sym.RP); }
					case -23:
						break;
					case 23:
						{return new Symbol(sym.LCBR); }
					case -24:
						break;
					case 24:
						{return new Symbol(sym.RCBR); }
					case -25:
						break;
					case 25:
						{return new Symbol(sym.QMARK);}
					case -26:
						break;
					case 26:
						{return new Symbol(sym.COMMA);}
					case -27:
						break;
					case 27:
						{return new Symbol(sym.SEMICOLON);}
					case -28:
						break;
					case 28:
						{}
					case -29:
						break;
					case 29:
						{ line_count++;}
					case -30:
						break;
					case 30:
						{}
					case -31:
						break;
					case 31:
						{yybegin(LINECOMMENT) ;}
					case -32:
						break;
					case 32:
						{yybegin(COMMENT) ;}
					case -33:
						break;
					case 33:
						{return new Symbol(sym.EQ);}
					case -34:
						break;
					case 34:
						{return new Symbol(sym.PRED); }
					case -35:
						break;
					case 35:
						{return new Symbol(sym.INS_PRED); }
					case -36:
						break;
					case 36:
						{return new Symbol(sym.CONSISTENCY_RULE );}
					case -37:
						break;
					case 37:
						{return new Symbol(sym.TITLE);}
					case -38:
						break;
					case 38:
						{return new Symbol(sym.SET);}
					case -39:
						break;
					case 39:
						{return new Symbol(sym.IN );}
					case -40:
						break;
					case 40:
						{return new Symbol(sym.TC);}
					case -41:
						break;
					case 41:
						{return new Symbol(sym.IMPLIES);}
					case -42:
						break;
					case 42:
						{return new Symbol(sym.NEQ);}
					case -43:
						break;
					case 43:
						{return new Symbol(sym.STRING, yytext().substring(1, yytext().length()-1));}
					case -44:
						break;
					case 44:
						{return new Symbol(sym.IMPLIES_T);}
					case -45:
						break;
					case 45:
						{return new Symbol(sym.END );}
					case -46:
						break;
					case 46:
						{return new Symbol(sym.LET );}
					case -47:
						break;
					case 47:
						{return new Symbol(sym.IFF);}
					case -48:
						break;
					case 48:
						{return new Symbol(sym.ARROW);}
					case -49:
						break;
					case 49:
						{return new Symbol(sym.UNKNOWN );}
					case -50:
						break;
					case 50:
						{return new Symbol(sym.NEW_COLON );}
					case -51:
						break;
					case 51:
						{return new Symbol(sym.COPY );}
					case -52:
						break;
					case 52:
						{return new Symbol(sym.BLUR );}
					case -53:
						break;
					case 53:
						{return new Symbol(sym.AUTO );}
					case -54:
						break;
					case 54:
						{return new Symbol(sym.JOIN );}
					case -55:
						break;
					case 55:
						{return new Symbol(sym.FOCUS );}
					case -56:
						break;
					case 56:
						{return new Symbol(sym.BEGIN );}
					case -57:
						break;
					case 57:
						{return new Symbol(sym.HALT_COLON );}
					case -58:
						break;
					case 58:
						{return new Symbol(sym.PREFAB );}
					case -59:
						break;
					case 59:
						{return new Symbol(sym.COERCE );}
					case -60:
						break;
					case 60:
						{return new Symbol(sym.CLONE_COLON );}
					case -61:
						break;
					case 61:
						{return new Symbol(sym.RETAIN_COLON );}
					case -62:
						break;
					case 62:
						{return new Symbol(sym.FOREACH );}
					case -63:
						break;
					case 63:
						{return new Symbol(sym.UPDATE_COLON );}
					case -64:
						break;
					case 64:
						{return new Symbol(sym.MESSAGE_COLON );}
					case -65:
						break;
					case 65:
						{return new Symbol(sym.MEETWITH );}
					case -66:
						break;
					case 66:
						{return new Symbol(sym.JOINWITH );}
					case -67:
						break;
					case 67:
						{return new Symbol(sym.FUNCTION );}
					case -68:
						break;
					case 68:
						{return new Symbol(sym.TRANSFORM );}
					case -69:
						break;
					case 69:
						{return new Symbol(sym.EMBEDBLUR );}
					case -70:
						break;
					case 70:
						{return new Symbol(sym.DIFF_OVER );}
					case -71:
						break;
					case 71:
						{return new Symbol(sym.DIFF_UNDER );}
					case -72:
						break;
					case 72:
						{return new Symbol(sym.MEETFOREACH );}
					case -73:
						break;
					case 73:
						{return new Symbol(sym.JOINFOREACH );}
					case -74:
						break;
					case 74:
						{return new Symbol(sym.PRECOND_COLON );}
					case -75:
						break;
					case 75:
						{}
					case -76:
						break;
					case 76:
						{line_count++;}
					case -77:
						break;
					case 77:
						{ yybegin(YYINITIAL) ;}
					case -78:
						break;
					case 78:
						{ addProperties(yytext()); }
					case -79:
						break;
					case 79:
						{}
					case -80:
						break;
					case 80:
						{line_count++; yybegin(YYINITIAL); }
					case -81:
						break;
					case 81:
						{ addProperties(yytext()); }
					case -82:
						break;
					case 83:
						{ Logger.println("Illegal character: "+yytext()); }
					case -83:
						break;
					case 84:
						{return new Symbol(sym.ID, yytext()); }
					case -84:
						break;
					case 85:
						{}
					case -85:
						break;
					case 86:
						{}
					case -86:
						break;
					case 88:
						{ Logger.println("Illegal character: "+yytext()); }
					case -87:
						break;
					case 89:
						{return new Symbol(sym.ID, yytext()); }
					case -88:
						break;
					case 90:
						{}
					case -89:
						break;
					case 92:
						{return new Symbol(sym.ID, yytext()); }
					case -90:
						break;
					case 94:
						{return new Symbol(sym.ID, yytext()); }
					case -91:
						break;
					case 96:
						{return new Symbol(sym.ID, yytext()); }
					case -92:
						break;
					case 98:
						{return new Symbol(sym.ID, yytext()); }
					case -93:
						break;
					case 100:
						{return new Symbol(sym.ID, yytext()); }
					case -94:
						break;
					case 102:
						{return new Symbol(sym.ID, yytext()); }
					case -95:
						break;
					case 104:
						{return new Symbol(sym.ID, yytext()); }
					case -96:
						break;
					case 106:
						{return new Symbol(sym.ID, yytext()); }
					case -97:
						break;
					case 108:
						{return new Symbol(sym.ID, yytext()); }
					case -98:
						break;
					case 110:
						{return new Symbol(sym.ID, yytext()); }
					case -99:
						break;
					case 112:
						{return new Symbol(sym.ID, yytext()); }
					case -100:
						break;
					case 114:
						{return new Symbol(sym.ID, yytext()); }
					case -101:
						break;
					case 116:
						{return new Symbol(sym.ID, yytext()); }
					case -102:
						break;
					case 118:
						{return new Symbol(sym.ID, yytext()); }
					case -103:
						break;
					case 120:
						{return new Symbol(sym.ID, yytext()); }
					case -104:
						break;
					case 122:
						{return new Symbol(sym.ID, yytext()); }
					case -105:
						break;
					case 124:
						{return new Symbol(sym.ID, yytext()); }
					case -106:
						break;
					case 126:
						{return new Symbol(sym.ID, yytext()); }
					case -107:
						break;
					case 128:
						{return new Symbol(sym.ID, yytext()); }
					case -108:
						break;
					case 130:
						{return new Symbol(sym.ID, yytext()); }
					case -109:
						break;
					case 132:
						{return new Symbol(sym.ID, yytext()); }
					case -110:
						break;
					case 134:
						{return new Symbol(sym.ID, yytext()); }
					case -111:
						break;
					case 136:
						{return new Symbol(sym.ID, yytext()); }
					case -112:
						break;
					case 138:
						{return new Symbol(sym.ID, yytext()); }
					case -113:
						break;
					case 140:
						{return new Symbol(sym.ID, yytext()); }
					case -114:
						break;
					case 142:
						{return new Symbol(sym.ID, yytext()); }
					case -115:
						break;
					case 155:
						{return new Symbol(sym.ID, yytext()); }
					case -116:
						break;
					case 156:
						{return new Symbol(sym.ID, yytext()); }
					case -117:
						break;
					case 157:
						{return new Symbol(sym.ID, yytext()); }
					case -118:
						break;
					case 158:
						{return new Symbol(sym.ID, yytext()); }
					case -119:
						break;
					case 159:
						{return new Symbol(sym.ID, yytext()); }
					case -120:
						break;
					case 160:
						{return new Symbol(sym.ID, yytext()); }
					case -121:
						break;
					case 161:
						{return new Symbol(sym.ID, yytext()); }
					case -122:
						break;
					case 162:
						{return new Symbol(sym.ID, yytext()); }
					case -123:
						break;
					case 163:
						{return new Symbol(sym.ID, yytext()); }
					case -124:
						break;
					case 164:
						{return new Symbol(sym.ID, yytext()); }
					case -125:
						break;
					case 165:
						{return new Symbol(sym.ID, yytext()); }
					case -126:
						break;
					case 166:
						{return new Symbol(sym.ID, yytext()); }
					case -127:
						break;
					case 167:
						{return new Symbol(sym.ID, yytext()); }
					case -128:
						break;
					case 168:
						{return new Symbol(sym.ID, yytext()); }
					case -129:
						break;
					case 169:
						{return new Symbol(sym.ID, yytext()); }
					case -130:
						break;
					case 170:
						{return new Symbol(sym.ID, yytext()); }
					case -131:
						break;
					case 171:
						{return new Symbol(sym.ID, yytext()); }
					case -132:
						break;
					case 172:
						{return new Symbol(sym.ID, yytext()); }
					case -133:
						break;
					case 173:
						{return new Symbol(sym.ID, yytext()); }
					case -134:
						break;
					case 174:
						{return new Symbol(sym.ID, yytext()); }
					case -135:
						break;
					case 175:
						{return new Symbol(sym.ID, yytext()); }
					case -136:
						break;
					case 176:
						{return new Symbol(sym.ID, yytext()); }
					case -137:
						break;
					case 177:
						{return new Symbol(sym.ID, yytext()); }
					case -138:
						break;
					case 178:
						{return new Symbol(sym.ID, yytext()); }
					case -139:
						break;
					case 179:
						{return new Symbol(sym.ID, yytext()); }
					case -140:
						break;
					case 180:
						{return new Symbol(sym.ID, yytext()); }
					case -141:
						break;
					case 181:
						{return new Symbol(sym.ID, yytext()); }
					case -142:
						break;
					case 182:
						{return new Symbol(sym.ID, yytext()); }
					case -143:
						break;
					case 183:
						{return new Symbol(sym.ID, yytext()); }
					case -144:
						break;
					case 184:
						{return new Symbol(sym.ID, yytext()); }
					case -145:
						break;
					case 185:
						{return new Symbol(sym.ID, yytext()); }
					case -146:
						break;
					case 186:
						{return new Symbol(sym.ID, yytext()); }
					case -147:
						break;
					case 187:
						{return new Symbol(sym.ID, yytext()); }
					case -148:
						break;
					case 188:
						{return new Symbol(sym.ID, yytext()); }
					case -149:
						break;
					case 189:
						{return new Symbol(sym.ID, yytext()); }
					case -150:
						break;
					case 190:
						{return new Symbol(sym.ID, yytext()); }
					case -151:
						break;
					case 191:
						{return new Symbol(sym.ID, yytext()); }
					case -152:
						break;
					case 192:
						{return new Symbol(sym.ID, yytext()); }
					case -153:
						break;
					case 193:
						{return new Symbol(sym.ID, yytext()); }
					case -154:
						break;
					case 194:
						{return new Symbol(sym.ID, yytext()); }
					case -155:
						break;
					case 195:
						{return new Symbol(sym.ID, yytext()); }
					case -156:
						break;
					case 196:
						{return new Symbol(sym.ID, yytext()); }
					case -157:
						break;
					case 197:
						{return new Symbol(sym.ID, yytext()); }
					case -158:
						break;
					case 198:
						{return new Symbol(sym.ID, yytext()); }
					case -159:
						break;
					case 199:
						{return new Symbol(sym.ID, yytext()); }
					case -160:
						break;
					case 200:
						{return new Symbol(sym.ID, yytext()); }
					case -161:
						break;
					case 201:
						{return new Symbol(sym.ID, yytext()); }
					case -162:
						break;
					case 202:
						{return new Symbol(sym.ID, yytext()); }
					case -163:
						break;
					case 203:
						{return new Symbol(sym.ID, yytext()); }
					case -164:
						break;
					case 204:
						{return new Symbol(sym.ID, yytext()); }
					case -165:
						break;
					case 205:
						{return new Symbol(sym.ID, yytext()); }
					case -166:
						break;
					case 206:
						{return new Symbol(sym.ID, yytext()); }
					case -167:
						break;
					case 207:
						{return new Symbol(sym.ID, yytext()); }
					case -168:
						break;
					case 208:
						{return new Symbol(sym.ID, yytext()); }
					case -169:
						break;
					case 209:
						{return new Symbol(sym.ID, yytext()); }
					case -170:
						break;
					case 210:
						{return new Symbol(sym.ID, yytext()); }
					case -171:
						break;
					case 211:
						{return new Symbol(sym.ID, yytext()); }
					case -172:
						break;
					case 212:
						{return new Symbol(sym.ID, yytext()); }
					case -173:
						break;
					case 213:
						{return new Symbol(sym.ID, yytext()); }
					case -174:
						break;
					case 214:
						{return new Symbol(sym.ID, yytext()); }
					case -175:
						break;
					case 215:
						{return new Symbol(sym.ID, yytext()); }
					case -176:
						break;
					case 216:
						{return new Symbol(sym.ID, yytext()); }
					case -177:
						break;
					case 217:
						{return new Symbol(sym.ID, yytext()); }
					case -178:
						break;
					case 218:
						{return new Symbol(sym.ID, yytext()); }
					case -179:
						break;
					case 219:
						{return new Symbol(sym.ID, yytext()); }
					case -180:
						break;
					case 220:
						{return new Symbol(sym.ID, yytext()); }
					case -181:
						break;
					case 221:
						{return new Symbol(sym.ID, yytext()); }
					case -182:
						break;
					case 222:
						{return new Symbol(sym.ID, yytext()); }
					case -183:
						break;
					case 223:
						{return new Symbol(sym.ID, yytext()); }
					case -184:
						break;
					case 224:
						{return new Symbol(sym.ID, yytext()); }
					case -185:
						break;
					case 225:
						{return new Symbol(sym.ID, yytext()); }
					case -186:
						break;
					case 226:
						{return new Symbol(sym.ID, yytext()); }
					case -187:
						break;
					case 227:
						{return new Symbol(sym.ID, yytext()); }
					case -188:
						break;
					case 228:
						{return new Symbol(sym.ID, yytext()); }
					case -189:
						break;
					case 229:
						{return new Symbol(sym.ID, yytext()); }
					case -190:
						break;
					case 230:
						{return new Symbol(sym.ID, yytext()); }
					case -191:
						break;
					case 231:
						{return new Symbol(sym.ID, yytext()); }
					case -192:
						break;
					case 232:
						{return new Symbol(sym.ID, yytext()); }
					case -193:
						break;
					case 233:
						{return new Symbol(sym.ID, yytext()); }
					case -194:
						break;
					case 234:
						{return new Symbol(sym.ID, yytext()); }
					case -195:
						break;
					case 235:
						{return new Symbol(sym.ID, yytext()); }
					case -196:
						break;
					case 236:
						{return new Symbol(sym.ID, yytext()); }
					case -197:
						break;
					case 237:
						{return new Symbol(sym.ID, yytext()); }
					case -198:
						break;
					case 238:
						{return new Symbol(sym.ID, yytext()); }
					case -199:
						break;
					case 239:
						{return new Symbol(sym.ID, yytext()); }
					case -200:
						break;
					case 240:
						{return new Symbol(sym.ID, yytext()); }
					case -201:
						break;
					case 241:
						{return new Symbol(sym.ID, yytext()); }
					case -202:
						break;
					case 242:
						{return new Symbol(sym.ID, yytext()); }
					case -203:
						break;
					case 243:
						{return new Symbol(sym.ID, yytext()); }
					case -204:
						break;
					case 244:
						{return new Symbol(sym.ID, yytext()); }
					case -205:
						break;
					case 245:
						{return new Symbol(sym.ID, yytext()); }
					case -206:
						break;
					case 246:
						{return new Symbol(sym.ID, yytext()); }
					case -207:
						break;
					case 247:
						{return new Symbol(sym.ID, yytext()); }
					case -208:
						break;
					case 248:
						{return new Symbol(sym.ID, yytext()); }
					case -209:
						break;
					case 249:
						{return new Symbol(sym.ID, yytext()); }
					case -210:
						break;
					case 250:
						{return new Symbol(sym.ID, yytext()); }
					case -211:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
