package icu.lowcoder.spring.cloud.organization.dao.spce;

import icu.lowcoder.spring.cloud.organization.entity.Authority;
import icu.lowcoder.spring.cloud.organization.entity.Employee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.UUID;

public class EmployeeSpecs {

    public static Specification<Employee> authoritiesIdAllMatch(List<UUID> ids) {
        return (root, query, builder) -> {
            if (ids != null && !ids.isEmpty()) {
                Subquery<UUID> authoritiesIdsQuery = query.subquery(UUID.class);
                Root<Employee> employeeRoot = authoritiesIdsQuery.from(Employee.class);
                authoritiesIdsQuery.select(employeeRoot.join("authorities", JoinType.LEFT).get("id"));
                authoritiesIdsQuery.where(builder.equal(root, employeeRoot));

                Predicate[] andPredicates = ids.stream()
                        .map(authorityId -> {
                            Subquery<UUID> idQuery = query.subquery(UUID.class);
                            Root<Authority> authorityRoot = idQuery.from(Authority.class);
                            idQuery.select(authorityRoot.get("id"));
                            idQuery.where(builder.equal(authorityRoot.get("id"), authorityId));
                            return builder.in(idQuery).value(authoritiesIdsQuery);
                        })
                        .toArray(Predicate[]::new);

                return builder.and(andPredicates);
            }
            return null;
        };
    }

    public static Specification<Employee> idIn(List<UUID> ids) {
        return (root, query, builder) -> {
            if (ids != null && !ids.isEmpty()) {
                CriteriaBuilder.In<UUID> in = builder.in(root.get("id"));
                for (UUID id : ids) {
                    in.value(id);
                }
                return in;
            }
            return null;
        };
    }

    public static Specification<Employee> keywordMatch(String keyword) {
        return (root, query, builder) -> {
            if (StringUtils.hasText(keyword)) {
                String v = "%" + keyword.trim() + "%";
                return builder.or(
                        builder.like(root.get("name"), v),
                        builder.like(root.get("no"), v),
                        builder.like(root.get("phone"), v),
                        builder.like(root.get("email"), v)
                );
            }
            return null;
        };
    }

    public static Specification<Employee> inDepartment(UUID departmentId) {
        return (root, query, builder) -> {
            if (departmentId != null) {
                return builder.equal(root.join("departments").get("id"), departmentId);
            }
            return null;
        };
    }
}
