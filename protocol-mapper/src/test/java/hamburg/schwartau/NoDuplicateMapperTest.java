package hamburg.schwartau;

import org.junit.jupiter.api.Test;
import org.keycloak.protocol.ProtocolMapper;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class NoDuplicateMapperTest {

    @Test
    public void shouldNotHaveMappersWithDuplicateIds() {
        final ServiceLoader<ProtocolMapper> serviceLoader = ServiceLoader.load(ProtocolMapper.class);
        final Collection<String> mapperIds = StreamSupport.stream(serviceLoader.spliterator(), false).map(elem -> elem.getId()).collect(Collectors.toList());

        assertThat(mapperIds).doesNotHaveDuplicates();
    }
}
