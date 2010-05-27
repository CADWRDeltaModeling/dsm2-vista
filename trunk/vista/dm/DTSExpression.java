/*
    Copyright (C) 1996-2000 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0
	by Nicky Sandhu
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA 95814
    (916)-653-7552
    nsandhu@water.ca.gov

    Send bug reports to nsandhu@water.ca.gov

    This program is licensed to you under the terms of the GNU General
    Public License, version 2, as published by the Free Software
    Foundation.

    You should have received a copy of the GNU General Public License
    along with this program; if not, contact Dr. Francis Chung, below,
    or the Free Software Foundation, 675 Mass Ave, Cambridge, MA
    02139, USA.

    THIS SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES AND CONTRIBUTORS "AS IS" AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES OR ITS CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
    OR SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR PROFITS; OR
    BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
    USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
    DAMAGE.

    For more information about VISTA, contact:

    Dr. Francis Chung
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA  95814
    916-653-5601
    chung@water.ca.gov

    or see our home page: http://wwwdelmod.water.ca.gov/

    Send bug reports to nsandhu@water.ca.gov or call (916)-653-7552

*/
/* Generated By:JavaCC: Do not edit this line. DTSExpression.java */
package vista.dm;

import vista.dm.syntaxtree.*;
import java.util.Vector;


public class DTSExpression implements DTSExpressionConstants {
   public static NodeFactory factory = new DefaultNodeFactory();

  static final public one_line one_line() throws ParseException {
   logical n0;
   NodeToken n1;
   Token n2;

   {
   }
    n0 = logical();
    n2 = jj_consume_token(0);
      n2.beginColumn++; n2.endColumn++;
      n1 = factory.createNodeToken(n2);
     {if (true) return factory.createone_line(n0,n1);}
    throw new Error("Missing return statement in function");
  }

  static final public logical logical() throws ParseException {
   relational n0;
   NodeListOptional n1 = factory.createNodeListOptional();
   NodeSequence n2;
   NodeChoice n3;
   NodeToken n4;
   Token n5;
   NodeToken n6;
   Token n7;
   NodeToken n8;
   Token n9;
   relational n10;

   {
   }
    n0 = relational();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
      case OR:
      case XOR:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
        n2 = factory.createNodeSequence(2);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        n5 = jj_consume_token(AND);
                       n4 = factory.createNodeToken(n5);
              n3 = factory.createNodeChoice(n4, 0);
        break;
      case OR:
        n7 = jj_consume_token(OR);
                      n6 = factory.createNodeToken(n7);
              n3 = factory.createNodeChoice(n6, 1);
        break;
      case XOR:
        n9 = jj_consume_token(XOR);
                       n8 = factory.createNodeToken(n9);
              n3 = factory.createNodeChoice(n8, 2);
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
        n2.addNode(n3);
      n10 = relational();
        n2.addNode(n10);
        n1.addNode(n2);
    }
     n1.nodes.trimToSize();
     {if (true) return factory.createlogical(n0,n1);}
    throw new Error("Missing return statement in function");
  }

  static final public relational relational() throws ParseException {
   sum n0;
   NodeOptional n1 = factory.createNodeOptional();
   NodeSequence n2;
   NodeChoice n3;
   NodeToken n4;
   Token n5;
   NodeToken n6;
   Token n7;
   NodeToken n8;
   Token n9;
   NodeToken n10;
   Token n11;
   NodeToken n12;
   Token n13;
   NodeToken n14;
   Token n15;
   sum n16;

   {
   }
    n0 = sum();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LT:
    case GT:
    case GE:
    case LE:
    case EQ:
    case NE:
        n2 = factory.createNodeSequence(2);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LT:
        n5 = jj_consume_token(LT);
                      n4 = factory.createNodeToken(n5);
              n3 = factory.createNodeChoice(n4, 0);
        break;
      case GT:
        n7 = jj_consume_token(GT);
                      n6 = factory.createNodeToken(n7);
              n3 = factory.createNodeChoice(n6, 1);
        break;
      case GE:
        n9 = jj_consume_token(GE);
                      n8 = factory.createNodeToken(n9);
              n3 = factory.createNodeChoice(n8, 2);
        break;
      case LE:
        n11 = jj_consume_token(LE);
                       n10 = factory.createNodeToken(n11);
              n3 = factory.createNodeChoice(n10, 3);
        break;
      case EQ:
        n13 = jj_consume_token(EQ);
                       n12 = factory.createNodeToken(n13);
              n3 = factory.createNodeChoice(n12, 4);
        break;
      case NE:
        n15 = jj_consume_token(NE);
                       n14 = factory.createNodeToken(n15);
              n3 = factory.createNodeChoice(n14, 5);
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
        n2.addNode(n3);
      n16 = sum();
        n2.addNode(n16);
        n1.addNode(n2);
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
     {if (true) return factory.createrelational(n0,n1);}
    throw new Error("Missing return statement in function");
  }

  static final public sum sum() throws ParseException {
   term n0;
   NodeListOptional n1 = factory.createNodeListOptional();
   NodeSequence n2;
   NodeChoice n3;
   NodeToken n4;
   Token n5;
   NodeToken n6;
   Token n7;
   term n8;

   {
   }
    n0 = term();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_2;
      }
        n2 = factory.createNodeSequence(2);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        n5 = jj_consume_token(PLUS);
                        n4 = factory.createNodeToken(n5);
              n3 = factory.createNodeChoice(n4, 0);
        break;
      case MINUS:
        n7 = jj_consume_token(MINUS);
                         n6 = factory.createNodeToken(n7);
              n3 = factory.createNodeChoice(n6, 1);
        break;
      default:
        jj_la1[5] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
        n2.addNode(n3);
      n8 = term();
        n2.addNode(n8);
        n1.addNode(n2);
    }
     n1.nodes.trimToSize();
     {if (true) return factory.createsum(n0,n1);}
    throw new Error("Missing return statement in function");
  }

  static final public term term() throws ParseException {
   exp n0;
   NodeListOptional n1 = factory.createNodeListOptional();
   NodeSequence n2;
   NodeChoice n3;
   NodeToken n4;
   Token n5;
   NodeToken n6;
   Token n7;
   exp n8;

   {
   }
    n0 = exp();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case MULTIPLY:
      case DIVIDE:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_3;
      }
        n2 = factory.createNodeSequence(2);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case MULTIPLY:
        n5 = jj_consume_token(MULTIPLY);
                            n4 = factory.createNodeToken(n5);
              n3 = factory.createNodeChoice(n4, 0);
        break;
      case DIVIDE:
        n7 = jj_consume_token(DIVIDE);
                          n6 = factory.createNodeToken(n7);
              n3 = factory.createNodeChoice(n6, 1);
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
        n2.addNode(n3);
      n8 = exp();
        n2.addNode(n8);
        n1.addNode(n2);
    }
     n1.nodes.trimToSize();
     {if (true) return factory.createterm(n0,n1);}
    throw new Error("Missing return statement in function");
  }

  static final public exp exp() throws ParseException {
   unary n0;
   NodeListOptional n1 = factory.createNodeListOptional();
   NodeSequence n2;
   NodeToken n3;
   Token n4;
   exp n5;

   {
   }
    n0 = unary();
    label_4:
    while (true) {
      if (jj_2_1(2147483647)) {
        ;
      } else {
        break label_4;
      }
        n2 = factory.createNodeSequence(2);
      n4 = jj_consume_token(EXP);
                 n3 = factory.createNodeToken(n4);
        n2.addNode(n3);
      n5 = exp();
        n2.addNode(n5);
        n1.addNode(n2);
    }
     n1.nodes.trimToSize();
     {if (true) return factory.createexp(n0,n1);}
    throw new Error("Missing return statement in function");
  }

  static final public unary unary() throws ParseException {
   NodeChoice n0;
   NodeSequence n1;
   NodeToken n2;
   Token n3;
   element n4;
   element n5;

   {
   }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case MINUS:
        n1 = factory.createNodeSequence(2);
      n3 = jj_consume_token(MINUS);
                   n2 = factory.createNodeToken(n3);
        n1.addNode(n2);
      n4 = element();
        n1.addNode(n4);
        n0 = factory.createNodeChoice(n1, 0);
      break;
    case CONSTANT:
    case VARIABLE:
    case ID:
    case 28:
      n5 = element();
        n0 = factory.createNodeChoice(n5, 1);
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
     {if (true) return factory.createunary(n0);}
    throw new Error("Missing return statement in function");
  }

  static final public element element() throws ParseException {
   NodeChoice n0;
   NodeToken n1;
   Token n2;
   NodeToken n3;
   Token n4;
   function n5;
   NodeSequence n6;
   NodeToken n7;
   Token n8;
   logical n9;
   NodeToken n10;
   Token n11;

   {
   }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CONSTANT:
      n2 = jj_consume_token(CONSTANT);
                      n1 = factory.createNodeToken(n2);
        n0 = factory.createNodeChoice(n1, 0);
      break;
    case VARIABLE:
      n4 = jj_consume_token(VARIABLE);
                      n3 = factory.createNodeToken(n4);
        n0 = factory.createNodeChoice(n3, 1);
      break;
    case ID:
      n5 = function();
        n0 = factory.createNodeChoice(n5, 2);
      break;
    case 28:
        n6 = factory.createNodeSequence(3);
      n8 = jj_consume_token(28);
               n7 = factory.createNodeToken(n8);
        n6.addNode(n7);
      n9 = logical();
        n6.addNode(n9);
      n11 = jj_consume_token(29);
                n10 = factory.createNodeToken(n11);
        n6.addNode(n10);
        n0 = factory.createNodeChoice(n6, 3);
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
     {if (true) return factory.createelement(n0);}
    throw new Error("Missing return statement in function");
  }

  static final public function function() throws ParseException {
   NodeToken n0;
   Token n1;
   NodeToken n2;
   Token n3;
   NodeOptional n4 = factory.createNodeOptional();
   NodeSequence n5;
   logical n6;
   NodeListOptional n7;
   NodeSequence n8;
   NodeToken n9;
   Token n10;
   NodeChoice n11;
   logical n12;
   NodeToken n13;
   Token n14;
   NodeToken n15;
   Token n16;

   {
   }
    n1 = jj_consume_token(ID);
             n0 = factory.createNodeToken(n1);
    n3 = jj_consume_token(28);
            n2 = factory.createNodeToken(n3);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case MINUS:
    case CONSTANT:
    case VARIABLE:
    case ID:
    case 28:
        n7 = factory.createNodeListOptional();
        n5 = factory.createNodeSequence(2);
      n6 = logical();
        n5.addNode(n6);
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 30:
          ;
          break;
        default:
          jj_la1[10] = jj_gen;
          break label_5;
        }
           n8 = factory.createNodeSequence(2);
        n10 = jj_consume_token(30);
                   n9 = factory.createNodeToken(n10);
           n8.addNode(n9);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case MINUS:
        case CONSTANT:
        case VARIABLE:
        case ID:
        case 28:
          n12 = logical();
                 n11 = factory.createNodeChoice(n12, 0);
          break;
        case INTERVAL:
          n14 = jj_consume_token(INTERVAL);
                                n13 = factory.createNodeToken(n14);
                 n11 = factory.createNodeChoice(n13, 1);
          break;
        default:
          jj_la1[11] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
           n8.addNode(n11);
           n7.addNode(n8);
      }
        n7.nodes.trimToSize();
        n5.addNode(n7);
        n4.addNode(n5);
      break;
    default:
      jj_la1[12] = jj_gen;
      ;
    }
    n16 = jj_consume_token(29);
             n15 = factory.createNodeToken(n16);
     {if (true) return factory.createfunction(n0,n2,n4,n15);}
    throw new Error("Missing return statement in function");
  }

  static final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_1();
    jj_save(0, xla);
    return retval;
  }

  static final private boolean jj_3_1() {
    if (jj_scan_token(EXP)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  static private boolean jj_initialized_once = false;
  static public DTSExpressionTokenManager token_source;
  static ASCII_CharStream jj_input_stream;
  static public Token token, jj_nt;
  static private int jj_ntk;
  static private Token jj_scanpos, jj_lastpos;
  static private int jj_la;
  static public boolean lookingAhead = false;
  static private boolean jj_semLA;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[13];
  static final private int[] jj_la1_0 = {0x1c00,0x1c00,0x7e000,0x7e000,0x60,0x60,0x180,0x180,0x14880040,0x14880000,0x40000000,0x16880040,0x14880040,};
  static final private JJCalls[] jj_2_rtns = new JJCalls[1];
  static private boolean jj_rescan = false;
  static private int jj_gc = 0;

  public DTSExpression(java.io.InputStream stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new DTSExpressionTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public DTSExpression(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new DTSExpressionTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public DTSExpression(DTSExpressionTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  You must");
      System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(DTSExpressionTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    return (jj_scanpos.kind != kind);
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector jj_expentries = new java.util.Vector();
  static private int[] jj_expentry;
  static private int jj_kind = -1;
  static private int[] jj_lasttokens = new int[100];
  static private int jj_endpos;

  static private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration enum = jj_expentries.elements(); enum.hasMoreElements();) {
        int[] oldentry = (int[])(enum.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  static final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[31];
    for (int i = 0; i < 31; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 13; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 31; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

  static final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
    }
    jj_rescan = false;
  }

  static final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}

class JTBToolkit {
   static NodeToken makeNodeToken(Token t) {
      NodeToken node = new NodeToken(t.image.intern(), t.kind, t.beginLine, t.beginColumn, t.endLine, t.endColumn);

      if ( t.specialToken == null )
         return node;

      Vector temp = new Vector();
      Token orig = t;

      while ( t.specialToken != null ) {
         t = t.specialToken;
         temp.addElement(new NodeToken(t.image.intern(), t.kind, t.beginLine, t.beginColumn, t.endLine, t.endColumn));
      }

      // Reverse the special token list
      for ( int i = temp.size() - 1; i >= 0; --i )
         node.addSpecial((NodeToken)temp.elementAt(i));

      node.trimSpecials();
      return node;
   }
}