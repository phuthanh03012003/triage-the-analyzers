package team.projectpulse.team;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.Section;

import java.util.Locale;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ResourceLock("java.util.Locale.default")
class TeamSpecsTest {

    @Mock
    Root<Team> root;
    @Mock
    CriteriaQuery<?> query;
    @Mock
    CriteriaBuilder criteriaBuilder;
    @Mock
    Path<String> name;
    @Mock
    Path<Section> section;
    @Mock
    Expression<String> lowercaseName;

    @Test
    void teamNameSearchUsesTheSamePatternUnderEnglishAndTurkishLocales() {
        given(this.root.<String>get("teamName")).willReturn(this.name);
        given(this.criteriaBuilder.lower(this.name)).willReturn(this.lowercaseName);

        Locale original = Locale.getDefault();
        try {
            for (Locale locale : new Locale[]{Locale.ENGLISH, Locale.forLanguageTag("tr-TR")}) {
                Locale.setDefault(locale);
                TeamSpecs.containsTeamName("IVAN").toPredicate(this.root, this.query, this.criteriaBuilder);
            }
        } finally {
            Locale.setDefault(original);
        }

        verify(this.criteriaBuilder, org.mockito.Mockito.times(2)).like(this.lowercaseName, "%ivan%");
    }

    @Test
    void sectionNameSearchUsesTheSameValueUnderEnglishAndTurkishLocales() {
        given(this.root.<Section>get("section")).willReturn(this.section);
        given(this.section.<String>get("sectionName")).willReturn(this.name);
        given(this.criteriaBuilder.lower(this.name)).willReturn(this.lowercaseName);

        Locale original = Locale.getDefault();
        try {
            for (Locale locale : new Locale[]{Locale.ENGLISH, Locale.forLanguageTag("tr-TR")}) {
                Locale.setDefault(locale);
                TeamSpecs.hasSectionName("SENIOR I").toPredicate(this.root, this.query, this.criteriaBuilder);
            }
        } finally {
            Locale.setDefault(original);
        }

        verify(this.criteriaBuilder, org.mockito.Mockito.times(2)).equal(this.lowercaseName, "senior i");
    }
}
