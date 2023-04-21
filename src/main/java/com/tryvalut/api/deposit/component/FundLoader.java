package com.tryvalut.api.deposit.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryvalut.api.deposit.dtos.DepositDTO;
import com.tryvalut.api.deposit.dtos.DepositOutputDTO;
import com.tryvalut.api.deposit.exception.DepositException;
import com.tryvalut.api.deposit.service.DepositService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class FundLoader {

    private final ResourceLoader resourceLoader;
    private final DepositService depositService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(FundLoader.class);


    @PostConstruct
    public void requestDeposit() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:input.txt");
        ArrayList<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug(line);

                DepositDTO depositDTO = objectMapper.readValue(line, DepositDTO.class);
                try {
                    depositService.save(depositDTO);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                    logger.error(objectMapper.writeValueAsString(new DepositOutputDTO(depositDTO.depositId(), depositDTO.customerId(), false)));
                    errors.add(e.getMessage());
                }
            }
            logger.debug("Total number of errors {}",errors.size());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }

//        testOutput(errors);

        // TODO:: Remove Later;


    }

    private void testOutput(ArrayList<Integer> errors) {
        Resource outputResource = resourceLoader.getResource("classpath:output.txt");
        ArrayList<DepositOutputDTO> depositOutputDTOS = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(outputResource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("input: {}",line);
                ObjectMapper ouputObjectMapper = new ObjectMapper();
                DepositOutputDTO depositOutputDTO = ouputObjectMapper.readValue(line, DepositOutputDTO.class);
                depositOutputDTOS.add(depositOutputDTO);

            }

        } catch (IOException e) {
            // Handle the exception
            logger.error(e.getMessage());
        }

        List<Integer> rejectedIds = depositOutputDTOS.stream().filter(d -> d.accepted() == false).map(d -> d.id()).collect(Collectors.toList());

        logger.debug("Total errors: {}",errors.size());

        Stream<Integer> errorStream = errors.stream().filter(e -> !rejectedIds.contains(e));
        if (errorStream.count() == 0) {
            logger.debug("Congrats resolved");
        } else {
            errorStream.forEach(e -> System.out.println(e));
        }
    }


}
