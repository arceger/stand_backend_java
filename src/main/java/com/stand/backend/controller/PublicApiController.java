package com.stand.backend.controller;

import com.stand.backend.dto.*;
import com.stand.backend.mapper.VehicleMapper;
import com.stand.backend.service.MarketplaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Marketplace Público", description = "Endpoints abertos ao público para consulta de veículos e envio de propostas/leads")
@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    private final MarketplaceService marketplaceService;

    public PublicApiController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Operation(
            summary = "Listar veículos do catálogo público",
            description = "Retorna lista de veículos publicados, com suporte a filtros opcionais por busca textual, marca, ano mínimo e preço máximo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de veículos recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleCardResponse.class))))
    })
    @GetMapping("/vehicles")
    public List<VehicleCardResponse> listVehicles(
            @Parameter(description = "Busca textual por título, marca ou modelo", example = "Civic")
            @RequestParam(required = false) String search,
            @Parameter(description = "Marca do veículo", example = "Honda")
            @RequestParam(required = false) String brand,
            @Parameter(description = "Ano de fabricação mínimo", example = "2020")
            @RequestParam(required = false) Integer minYear,
            @Parameter(description = "Preço máximo", example = "150000.00")
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return marketplaceService.listPublished(new VehicleFilter(search, brand, minYear, maxPrice, null, false))
                .stream()
                .map(VehicleMapper::card)
                .toList();
    }

    @Operation(
            summary = "Listar veículos em destaque",
            description = "Retorna os veículos marcados como destaque (featured) para a vitrine principal da home."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de veículos em destaque recuperada com sucesso",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleCardResponse.class))))
    })
    @GetMapping("/vehicles/featured")
    public List<VehicleCardResponse> featuredVehicles() {
        return marketplaceService.featuredVehicles().stream().map(VehicleMapper::card).toList();
    }

    @Operation(
            summary = "Obter detalhes de um veículo por slug",
            description = "Retorna a ficha técnica completa e a galeria de imagens do veículo através do seu slug amigável."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes do veículo recuperados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/vehicles/{slug}")
    @Transactional(readOnly = true)
    public VehicleDetailResponse vehicleDetail(
            @Parameter(description = "Slug amigável do veículo", example = "honda-civic-2022-exl")
            @PathVariable String slug
    ) {
        return VehicleMapper.detail(marketplaceService.getPublishedBySlug(slug));
    }

    @Operation(
            summary = "Enviar proposta / lead de interesse",
            description = "Registra os dados de contato e mensagem de interesse de um cliente para o veículo especificado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lead registrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da proposta inválidos ou campos obrigatórios ausentes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/vehicles/{slug}/leads")
    public ResponseEntity<MessageResponse> createLead(
            @Parameter(description = "Slug amigável do veículo", example = "honda-civic-2022-exl")
            @PathVariable String slug,
            @RequestBody LeadFormRequest request
    ) {
        marketplaceService.createLead(slug, new LeadRequest(
                request.customerName(),
                request.phone(),
                request.email(),
                request.message()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Interesse enviado com sucesso. A loja entrara em contato em breve."));
    }
}
