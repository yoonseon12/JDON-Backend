package kernel.jdon.modulecrawler.domain.jd.core.dto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WantedJobListResponse {

    private List<Data> data;

    public Set<Long> toLinkedHashSet() {
        return this.data.stream()
            .map(Data::getId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Data {
        private Long id;
    }
}
