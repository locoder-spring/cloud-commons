package icu.lowcoder.spring.cloud.organization.service;

import icu.lowcoder.spring.cloud.organization.dto.request.AddEmployeeRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.EditEmployeeRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.GetEmployeeParams;
import icu.lowcoder.spring.cloud.organization.dto.request.ListEmployeesParams;
import icu.lowcoder.spring.cloud.organization.dto.response.EmployeeAuthoritiesDetail;
import icu.lowcoder.spring.cloud.organization.dto.response.EmployeeDetail;
import icu.lowcoder.spring.cloud.organization.dto.response.EmployeeListItem;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RequestMapping("/employees")
public interface EmployeesService {

    @GetMapping
    Page<EmployeeListItem> page(ListEmployeesParams params, Pageable pageable);

    @PostMapping
    UUIDIdResponse add(AddEmployeeRequest request);

    @PutMapping("/{employeeId}")
    void edit(@PathVariable UUID employeeId, EditEmployeeRequest request);

    @GetMapping(params = "op=findOneWithParams")
    EmployeeListItem getEmployee(@Valid GetEmployeeParams params);

    @DeleteMapping("/{employeeId}")
    void del(@PathVariable UUID employeeId);

    @GetMapping("/{employeeId}")
    EmployeeDetail get(@PathVariable UUID employeeId);

    @GetMapping(value = "/{employeeId}", params = "byIdAndPosition")
    EmployeeDetail getByIdAndPosition(@PathVariable UUID employeeId, @RequestParam String positionCode);

    @GetMapping(value = "/{accountId}", params = "byAccountId")
    EmployeeAuthoritiesDetail getByAccountId(@PathVariable UUID accountId);

    @GetMapping(value = "/{employeeId}", params = "byEmployeeId")
    EmployeeAuthoritiesDetail getByEmployeeId(@PathVariable UUID employeeId);

    @GetMapping(value = "/{authorityCode}", params = "byAuthorityCode")
    List<EmployeeListItem> listByAuthorityCode(@PathVariable String authorityCode);
}
