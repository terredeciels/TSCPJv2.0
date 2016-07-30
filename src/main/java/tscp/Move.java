package tscp;

/**
 * 1	capture
 * 2	castle
 * 4	en passant capture
 * 8	pushing a pawn 2 squares
 * 16	pawn move
 * 32	promote
 */
public class Move {

   public byte from;
   public byte to;
   public byte promote;
   public byte bits;

   public Move() {
    }

   public Move(byte from, byte to, byte promote, byte bits) {
        this.from = from;
        this.to = to;
        this.promote = promote;
        this.bits = bits;
    }

}
