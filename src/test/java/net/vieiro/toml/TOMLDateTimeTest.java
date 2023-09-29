/*
 * Copyright 2023 Antonio Vieiro <antonio@vieiro.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vieiro.toml;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLDateTimeTest {

    @Test
    public void testShouldParseOffsetDateTimProperly() throws Exception {
        System.out.println("testShouldParseOffsetDateTimProperly");
        TOML toml = Util.parse("date-test.toml", false);

        OffsetDateTime odt1 = toml.getOffsetDateTime("odt1").orElse(null);
        assertEquals(OffsetDateTime.parse("1979-05-27T07:32:00Z"), odt1);

        OffsetDateTime odt2 = toml.getOffsetDateTime("odt2").orElse(null);
        assertEquals(OffsetDateTime.parse("1979-05-27T00:32:00-07:00"), odt2);

        LocalDateTime ldt1 = toml.getLocalDateTime("ldt1").orElse(null);
        assertEquals(LocalDateTime.parse("1979-05-27T07:32:00"), ldt1);

        LocalDate ld1 = toml.getLocalDate("ld1").orElse(null);
        assertEquals(LocalDate.parse("1979-05-27"), ld1);

        LocalTime lt1 = toml.getLocalTime("lt1").orElse(null);
        assertEquals(LocalTime.parse("07:32:00"), lt1);

    }

}
