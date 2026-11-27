package kaamverse.bl.model;

/**
 * UC2: a verification request together with the maid it is about.
 * GRASP Information Expert: the request knows its decision, the maid knows
 * the details the officer compares (name, father name, CNIC, address, image).
 */
public record VerificationCase(VerificationRequest request, Maid maid) {
}
