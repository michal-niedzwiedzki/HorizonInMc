package pl.epsi.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static pl.epsi.render.CustomTextRenderer.Cursor;

class CustomTextRendererTest {

    final int CHAR_WIDTH = 10;
    final int CHAR_HEIGHT = 16;
    final int H_SPACING = 1;
    final int V_SPACING = 2;

    @Test
    void nextCursor_when_inside_region_do_move_to_the_right() {
        Cursor cursor = new CustomTextRenderer.Cursor(0, 0, true);
        Cursor next = CustomTextRenderer.nextCursor(cursor, CHAR_WIDTH, CHAR_HEIGHT, H_SPACING, V_SPACING, 0, 0, 100, 100);
        assertEquals(CHAR_WIDTH + H_SPACING, next.x); // move right
        assertEquals(0, next.y); // stay on the same line
        assertTrue(next.inside);
    }

    @Test
    void nextCursor_when_outside_horizontally_do_move_down_and_reset_horizontally() {
        Cursor cursor = new CustomTextRenderer.Cursor(102, 0, true);
        Cursor next = CustomTextRenderer.nextCursor(cursor, CHAR_WIDTH, CHAR_HEIGHT, H_SPACING, V_SPACING, 0, 0, 100, 100);
        assertEquals(0, next.x); // reset horizontally
        assertEquals(CHAR_HEIGHT + V_SPACING, next.y); // move down
        assertTrue(next.inside);
    }

    @Test
    void nextCursor_when_outside_vertically_do_move_right() {
        Cursor cursor = new CustomTextRenderer.Cursor(50, 102, true);
        Cursor next = CustomTextRenderer.nextCursor(cursor, CHAR_WIDTH, CHAR_HEIGHT, H_SPACING, V_SPACING, 0, 0, 100, 100);
        assertEquals(61, next.x); // move right
        assertEquals(102, next.y); // stay on the same line
        assertFalse(next.inside);
    }

    @Test
    void nextCursor_when_outside_both_horizontally_and_vertically_do_move_down_and_reset_horizontally() {
        Cursor cursor = new CustomTextRenderer.Cursor(102, 102, true);
        Cursor next = CustomTextRenderer.nextCursor(cursor, CHAR_WIDTH, CHAR_HEIGHT, H_SPACING, V_SPACING, 0, 0, 100, 100);
        assertEquals(0, next.x); // reset horizontally
        assertEquals(120, next.y); // move down
        assertFalse(next.inside);
    }

    @Test
    void nextCursor_when_break_line_do_move_down_and_reset_horizontally() {
        Cursor cursor = new CustomTextRenderer.Cursor(102, 0, true);
        Cursor next = CustomTextRenderer.nextCursor(cursor, 0, CHAR_HEIGHT, H_SPACING, V_SPACING, 0, 0, 100, 100);
        assertEquals(0, next.x); // reset horizontally
        assertEquals(CHAR_HEIGHT + V_SPACING, next.y); // move down
        assertTrue(next.inside);
    }

}
