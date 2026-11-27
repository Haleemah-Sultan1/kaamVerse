package kaamverse.bl.model;

/** UC10: a funding request together with the maid who raised it, as shown to the contributor. */
public record FundingCase(FundingRequest request, Maid maid) {
}
